package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.CartCheckoutRequestDto;
import com.jewelleryapp.dto.request.OrderRequestDto;
import com.jewelleryapp.dto.response.OrderResponseDto;
import com.jewelleryapp.entity.*;
import com.jewelleryapp.enums.OrderStatus;
import com.jewelleryapp.exception.InvalidRequestException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.OrderMapper;
import com.jewelleryapp.repository.*;
import com.jewelleryapp.service.notification.JewelleryNotificationService;
import com.jewelleryapp.service.OrderService;
import com.jewelleryapp.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final StockItemRepository stockItemRepository;
    private final StoreRepository storeRepository;
    private final CartRepository cartRepository;
    private final OrderMapper orderMapper;

    // --- INTEGRATION: Notification Service ---
    private final JewelleryNotificationService notificationService;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        return processOrderCreation(
                request.getItems(),
                request.getFulfillmentStoreId(),
                request.getShippingAddress(),
                request.getBillingAddress()
        );
    }

    @Override
    @Transactional
    public OrderResponseDto checkoutCart(CartCheckoutRequestDto request) {
        User user = getCurrentUserEntity();
        ShoppingCart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user."));

        if (cart.getItems().isEmpty()) {
            throw new InvalidRequestException("Cannot checkout an empty cart.");
        }

        List<OrderRequestDto.OrderItemRequestDto> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    OrderRequestDto.OrderItemRequestDto itemDto = new OrderRequestDto.OrderItemRequestDto();
                    itemDto.setProductId(cartItem.getProduct().getId());
                    itemDto.setQuantity(cartItem.getQuantity());
                    return itemDto;
                })
                .collect(Collectors.toList());

        OrderResponseDto response = processOrderCreation(
                orderItems,
                request.getFulfillmentStoreId(),
                request.getShippingAddress(),
                request.getBillingAddress()
        );

        cart.getItems().clear();
        cartRepository.save(cart);
        return response;
    }

    private OrderResponseDto processOrderCreation(List<OrderRequestDto.OrderItemRequestDto> items, UUID fulfillmentStoreId, String shippingAddress, String billingAddress) {
        User user = getCurrentUserEntity();

        CustomerOrder order = CustomerOrder.builder()
                .user(user)
                .orderNumber(generateOrderNumber())
                .status(OrderStatus.PAYMENT_PENDING)
                .shippingAddress(shippingAddress)
                .billingAddress(billingAddress)
                .items(new ArrayList<>())
                .statusHistory(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .build();

        Store fulfillmentStore;
        if (fulfillmentStoreId != null) {
            fulfillmentStore = storeRepository.findById(fulfillmentStoreId)
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", fulfillmentStoreId));
            validateStockForPickup(items, fulfillmentStore);
        } else {
            if (shippingAddress == null || shippingAddress.isBlank()) {
                throw new InvalidRequestException("Shipping address is required for delivery orders.");
            }
            fulfillmentStore = findBestFulfillmentSource(items);
        }
        order.setFulfillmentStore(fulfillmentStore);

        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequestDto.OrderItemRequestDto itemReq : items) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemReq.getProductId()));

            if (!product.isActive()) {
                throw new InvalidRequestException("Product '" + product.getName() + "' is currently unavailable.");
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .priceAtTimeOfPurchase(product.getBasePrice())
                    .build();

            order.getItems().add(orderItem);
            total = total.add(product.getBasePrice().multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalPrice(total);
        addStatusHistory(order, null, OrderStatus.PAYMENT_PENDING, "Order Created");

        // FIX: Use saveAndFlush so timestamps (createdAt) are populated immediately for the response
        CustomerOrder savedOrder = orderRepository.saveAndFlush(order);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public OrderResponseDto confirmOrderPayment(UUID orderId, String paymentMethod) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            // If already processing/shipped, strictly return current state to avoid errors
            if (order.getStatus() == OrderStatus.PROCESSING || order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
                return orderMapper.toDto(order);
            }
            throw new InvalidRequestException("Order is not in a valid state to receive payment.");
        }

        return processStatusUpdate(order, OrderStatus.PROCESSING, "Payment Verified via " + paymentMethod);
    }

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(UUID orderId, OrderStatus newStatus, String notes) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User currentUser = getCurrentUserEntity();
        boolean isManager = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_STORE_MANAGER"));
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));

        if (!isManager && !isAdmin) {
            throw new AccessDeniedException("Insufficient permissions to update order status.");
        }

        if (isManager) {
            if (order.getFulfillmentStore() == null ||
                    !order.getFulfillmentStore().getId().equals(currentUser.getManagedStore().getId())) {
                throw new AccessDeniedException("You can only manage orders for your assigned store.");
            }
        }

        return processStatusUpdate(order, newStatus, notes);
    }

    private OrderResponseDto processStatusUpdate(CustomerOrder order, OrderStatus newStatus, String notes) {
        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) return orderMapper.toDto(order);

        try {
            if (newStatus == OrderStatus.PROCESSING && oldStatus == OrderStatus.PAYMENT_PENDING) {
                deductStockForOrder(order);
                notificationService.sendOrderConfirmation(order);
            }

            boolean wasStockDeducted = (oldStatus == OrderStatus.PROCESSING || oldStatus == OrderStatus.SHIPPED || oldStatus == OrderStatus.DELIVERED);
            if ((newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.RETURNED) && wasStockDeducted) {
                restoreStockForOrder(order);
                if (newStatus == OrderStatus.CANCELLED) {
                    notificationService.sendOrderCancelled(order, notes != null ? notes : "Cancelled by System");
                }
            }
        } catch (OptimisticLockingFailureException e) {
            throw new InvalidRequestException("Inventory was updated by another user simultaneously. Please refresh and try again.");
        }

        order.setStatus(newStatus);
        addStatusHistory(order, oldStatus, newStatus, notes);

        // FIX: Use saveAndFlush for immediate timestamp updates
        return orderMapper.toDto(orderRepository.saveAndFlush(order));
    }

    private void deductStockForOrder(CustomerOrder order) {
        updateStock(order, -1);
    }

    private void restoreStockForOrder(CustomerOrder order) {
        updateStock(order, 1);
    }

    private void updateStock(CustomerOrder order, int multiplier) {
        UUID storeId = (order.getFulfillmentStore() != null) ? order.getFulfillmentStore().getId() : null;
        String locationName = (storeId == null) ? "Central Warehouse" : order.getFulfillmentStore().getName();

        for (OrderItem item : order.getItems()) {
            StockItem stockItem;
            if (storeId == null) {
                stockItem = stockItemRepository.findByProductIdAndStoreIdIsNull(item.getProduct().getId())
                        .orElseThrow(() -> new InvalidRequestException("Stock record missing in " + locationName));
            } else {
                stockItem = stockItemRepository.findByProductIdAndStoreId(item.getProduct().getId(), storeId)
                        .orElseThrow(() -> new InvalidRequestException("Stock record missing in " + locationName));
            }

            int change = item.getQuantity() * multiplier;

            if (change < 0 && stockItem.getQuantity() < Math.abs(change)) {
                throw new InvalidRequestException("Insufficient stock in " + locationName + " for product " + item.getProduct().getSku());
            }

            stockItem.setQuantity(stockItem.getQuantity() + change);
            stockItemRepository.save(stockItem);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrders(
            OrderStatus status, String orderNumber, BigDecimal minPrice, BigDecimal maxPrice,
            LocalDateTime startDate, LocalDateTime endDate, UUID userIdFilter, Pageable pageable) {

        User currentUser = getCurrentUserEntity();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isManager = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_STORE_MANAGER"));

        Specification<CustomerOrder> spec = Specification.where(OrderSpecification.hasStatus(status))
                .and(OrderSpecification.hasOrderNumber(orderNumber))
                .and(OrderSpecification.priceBetween(minPrice, maxPrice))
                .and(OrderSpecification.createdAfter(startDate))
                .and(OrderSpecification.createdBefore(endDate));

        if (isAdmin) {
            if (userIdFilter != null) spec = spec.and(OrderSpecification.hasUserId(userIdFilter));
        } else if (isManager) {
            if (currentUser.getManagedStore() == null) return Page.empty();
            spec = spec.and(OrderSpecification.hasFulfillmentStoreId(currentUser.getManagedStore().getId()));
        } else {
            spec = spec.and(OrderSpecification.hasUserId(currentUser.getId()));
        }

        return orderRepository.findAll(spec, pageable).map(orderMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID id) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        User currentUser = getCurrentUserEntity();
        boolean isAdmin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        boolean isManager = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_STORE_MANAGER"));

        if (isAdmin) return orderMapper.toDto(order);
        if (isManager) {
            if (order.getFulfillmentStore() != null &&
                    order.getFulfillmentStore().getId().equals(currentUser.getManagedStore().getId())) {
                return orderMapper.toDto(order);
            }
            throw new AccessDeniedException("This order is not assigned to your store.");
        }
        if (!order.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this order.");
        }
        return orderMapper.toDto(order);
    }

    private Store findBestFulfillmentSource(List<OrderRequestDto.OrderItemRequestDto> items) {
        if (canFulfillOrder(items, null)) return null;
        List<Store> allStores = storeRepository.findAll();
        for (Store store : allStores) {
            if (canFulfillOrder(items, store.getId())) return store;
        }
        throw new InvalidRequestException("One or more items are out of stock.");
    }

    private boolean canFulfillOrder(List<OrderRequestDto.OrderItemRequestDto> items, UUID storeId) {
        for (OrderRequestDto.OrderItemRequestDto item : items) {
            Optional<StockItem> stock;
            if (storeId == null) {
                stock = stockItemRepository.findByProductIdAndStoreIdIsNull(item.getProductId());
            } else {
                stock = stockItemRepository.findByProductIdAndStoreId(item.getProductId(), storeId);
            }
            if (stock.isEmpty() || stock.get().getQuantity() < item.getQuantity()) {
                return false;
            }
        }
        return true;
    }

    private void validateStockForPickup(List<OrderRequestDto.OrderItemRequestDto> items, Store store) {
        if (!canFulfillOrder(items, store.getId())) {
            throw new InvalidRequestException("Selected store does not have enough stock.");
        }
    }

    private void addStatusHistory(CustomerOrder order, OrderStatus from, OrderStatus to, String notes) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order).fromStatus(from).toStatus(to).notes(notes).build();
        order.getStatusHistory().add(history);
    }

    private User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new AccessDeniedException("User not authenticated"));
    }

    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}