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
import com.jewelleryapp.service.OrderService;
import com.jewelleryapp.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final CartRepository cartRepository; // Inject Cart Repo
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponseDto createOrder(OrderRequestDto request) {
        // Standard flow: User provides the list of items explicitly
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

        // 1. Fetch Cart
        ShoppingCart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user."));

        if (cart.getItems().isEmpty()) {
            throw new InvalidRequestException("Cannot checkout an empty cart.");
        }

        // 2. Map Cart Items to Order Request Format
        List<OrderRequestDto.OrderItemRequestDto> orderItems = cart.getItems().stream()
                .map(cartItem -> {
                    OrderRequestDto.OrderItemRequestDto itemDto = new OrderRequestDto.OrderItemRequestDto();
                    itemDto.setProductId(cartItem.getProduct().getId());
                    itemDto.setQuantity(cartItem.getQuantity());
                    return itemDto;
                })
                .collect(Collectors.toList());

        // 3. Create Order
        OrderResponseDto response = processOrderCreation(
                orderItems,
                request.getFulfillmentStoreId(),
                request.getShippingAddress(),
                request.getBillingAddress()
        );

        // 4. Clear Cart (Atomic operation: only happens if order creation succeeds)
        cart.getItems().clear();
        cartRepository.save(cart);
        log.info("Cart cleared for user {} after successful checkout.", user.getEmail());

        return response;
    }

    // --- Core Logic Refactored to be Reusable ---
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

        // Determine Fulfillment
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

        String sourceName = (fulfillmentStore == null) ? "Central Warehouse" : fulfillmentStore.getName();
        String typeName = (fulfillmentStoreId != null) ? "Pickup" : "Delivery";

        addStatusHistory(order, null, OrderStatus.PAYMENT_PENDING,
                "Order Created. Method: " + typeName + ", Source: " + sourceName);

        return orderMapper.toDto(orderRepository.save(order));
    }

    // --- Helper Methods (Same as before) ---

    private Store findBestFulfillmentSource(List<OrderRequestDto.OrderItemRequestDto> items) {
        if (canFulfillOrder(items, null)) {
            log.info("Routing to Central Warehouse");
            return null;
        }
        List<Store> allStores = storeRepository.findAll();
        for (Store store : allStores) {
            if (canFulfillOrder(items, store.getId())) {
                log.info("Routing to Store: {}", store.getName());
                return store;
            }
        }
        throw new InvalidRequestException("One or more items are out of stock and cannot be fulfilled from any location.");
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
            throw new InvalidRequestException("Selected store does not have enough stock for all items.");
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

    @Override
    @Transactional
    public OrderResponseDto updateOrderStatus(UUID orderId, OrderStatus newStatus, String notes) {
        CustomerOrder order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        User currentUser = getCurrentUserEntity();
        boolean isManager = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("ROLE_STORE_MANAGER"));

        if (isManager) {
            if (order.getFulfillmentStore() == null ||
                    !order.getFulfillmentStore().getId().equals(currentUser.getManagedStore().getId())) {
                throw new AccessDeniedException("You can only manage orders for your assigned store.");
            }
        }

        OrderStatus oldStatus = order.getStatus();
        if (oldStatus == newStatus) return orderMapper.toDto(order);

        if (newStatus == OrderStatus.PROCESSING && oldStatus == OrderStatus.PAYMENT_PENDING) {
            deductStockForOrder(order);
        }

        order.setStatus(newStatus);
        addStatusHistory(order, oldStatus, newStatus, notes);

        return orderMapper.toDto(orderRepository.save(order));
    }

    private void deductStockForOrder(CustomerOrder order) {
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

            if (stockItem.getQuantity() < item.getQuantity()) {
                throw new InvalidRequestException("Insufficient stock in " + locationName + ". Cannot process order.");
            }

            stockItem.setQuantity(stockItem.getQuantity() - item.getQuantity());
            stockItemRepository.save(stockItem);
            log.info("Stock deducted from {} for product {}", locationName, item.getProduct().getSku());
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