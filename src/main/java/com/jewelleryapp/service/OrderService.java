package com.jewelleryapp.service;

import com.jewelleryapp.dto.request.CartCheckoutRequestDto;
import com.jewelleryapp.dto.request.OrderRequestDto;
import com.jewelleryapp.dto.response.OrderResponseDto;
import com.jewelleryapp.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderService {

    // Standard single/list buy
    OrderResponseDto createOrder(OrderRequestDto request);

    // NEW: Buy everything in cart
    OrderResponseDto checkoutCart(CartCheckoutRequestDto request);

    Page<OrderResponseDto> getAllOrders(
            OrderStatus status,
            String orderNumber,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            LocalDateTime startDate,
            LocalDateTime endDate,
            UUID userIdFilter,
            Pageable pageable
    );

    OrderResponseDto getOrderById(UUID id);

    OrderResponseDto updateOrderStatus(UUID orderId, OrderStatus newStatus, String notes);
}