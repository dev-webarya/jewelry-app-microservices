package com.jewellery.orderservice.service;

import com.jewellery.common.exception.InvalidRequestException;
import com.jewellery.common.exception.ResourceNotFoundException;
import com.jewellery.orderservice.client.InventoryClient;
import com.jewellery.orderservice.client.ProductClient;
import com.jewellery.orderservice.dto.CreateOrderRequest;
import com.jewellery.orderservice.dto.OrderResponseDto;
import com.jewellery.orderservice.entity.CustomerOrder;
import com.jewellery.orderservice.entity.OrderItem;
import com.jewellery.orderservice.enums.OrderStatus;
import com.jewellery.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final InventoryClient inventoryClient;
    private final ProductClient productClient;

    @Transactional
    public OrderResponseDto createOrder(CreateOrderRequest request) {
        // Calculate total
        BigDecimal total = request.getItems().stream()
                .map(item -> item.getPriceAtTimeOfPurchase().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Generate order number
        String orderNumber = "ORD-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();

        CustomerOrder order = CustomerOrder.builder()
                .orderNumber(orderNumber)
                .userId(request.getUserId())
                .fulfillmentStoreId(request.getFulfillmentStoreId())
                .status(OrderStatus.PAYMENT_PENDING)
                .totalPrice(total)
                .shippingAddress(request.getShippingAddress())
                .billingAddress(request.getBillingAddress())
                .build();

        List<OrderItem> items = request.getItems().stream()
                .map(itemReq -> OrderItem.builder()
                        .order(order)
                        .productId(itemReq.getProductId())
                        .quantity(itemReq.getQuantity())
                        .priceAtTimeOfPurchase(itemReq.getPriceAtTimeOfPurchase())
                        .build())
                .collect(Collectors.toList());
        order.setItems(items);

        // Reserve stock via Feign (with graceful degradation)
        try {
            for (var item : items) {
                inventoryClient.reserveStock(new InventoryClient.StockReservationRequest(
                        item.getProductId(), request.getFulfillmentStoreId(), item.getQuantity()));
            }
        } catch (Exception e) {
            log.warn("Could not reserve stock: {}", e.getMessage());
            // Continue - stock validation might be done at checkout confirmation
        }

        return mapToDto(orderRepository.save(order));
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderById(UUID id) {
        return mapToDto(orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id)));
    }

    @Transactional(readOnly = true)
    public OrderResponseDto getOrderByNumber(String orderNumber) {
        return mapToDto(orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber)));
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDto> getOrdersByUserId(UUID userId) {
        return orderRepository.findByUserId(userId).stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderResponseDto> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable).map(this::mapToDto);
    }

    @Transactional
    public OrderResponseDto updateOrderStatus(UUID id, OrderStatus newStatus) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Validate status transition
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);

        // Release stock on cancellation
        if (newStatus == OrderStatus.CANCELLED) {
            try {
                for (var item : order.getItems()) {
                    inventoryClient.releaseStock(new InventoryClient.StockReservationRequest(
                            item.getProductId(), order.getFulfillmentStoreId(), item.getQuantity()));
                }
            } catch (Exception e) {
                log.warn("Could not release stock on cancellation: {}", e.getMessage());
            }
        }

        return mapToDto(orderRepository.save(order));
    }

    @Transactional
    public OrderResponseDto confirmPayment(UUID id, String paymentMethod, String transactionId) {
        CustomerOrder order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidRequestException("Order is not pending payment");
        }

        order.setStatus(OrderStatus.PROCESSING);
        return mapToDto(orderRepository.save(order));
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        // Simple validation - can be expanded
        if (current == OrderStatus.CANCELLED || current == OrderStatus.DELIVERED) {
            throw new InvalidRequestException("Cannot update status of a " + current + " order");
        }
    }

    private OrderResponseDto mapToDto(CustomerOrder order) {
        var items = order.getItems().stream().map(item -> {
            String productName = "Product";
            try {
                var resp = productClient.getProductById(item.getProductId());
                if (resp != null && resp.getData() != null) {
                    productName = resp.getData().name();
                }
            } catch (Exception e) {
                /* ignore */ }

            return OrderResponseDto.OrderItemDto.builder()
                    .id(item.getId()).productId(item.getProductId())
                    .productName(productName).quantity(item.getQuantity())
                    .priceAtTimeOfPurchase(item.getPriceAtTimeOfPurchase())
                    .build();
        }).collect(Collectors.toList());

        return OrderResponseDto.builder()
                .id(order.getId()).orderNumber(order.getOrderNumber())
                .userId(order.getUserId()).fulfillmentStoreId(order.getFulfillmentStoreId())
                .status(order.getStatus()).totalPrice(order.getTotalPrice())
                .shippingAddress(order.getShippingAddress()).billingAddress(order.getBillingAddress())
                .items(items).createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt())
                .build();
    }
}
