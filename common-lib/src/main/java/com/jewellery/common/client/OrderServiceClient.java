package com.jewellery.common.client;

import com.jewellery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Feign client for Order Service
 * Used by Payment and Shipping services
 */
@FeignClient(name = "order-service", path = "/api/v1/orders")
public interface OrderServiceClient {

    @GetMapping("/{id}")
    ApiResponse<OrderDto> getOrderById(@PathVariable UUID id);

    @PostMapping("/{id}/confirm-payment")
    ApiResponse<OrderDto> confirmPayment(@PathVariable UUID id, @RequestBody PaymentConfirmRequest request);

    record OrderDto(
            UUID id,
            String orderNumber,
            UUID userId,
            String status,
            BigDecimal totalPrice,
            String shippingAddress,
            LocalDateTime createdAt,
            List<OrderItemDto> items) {
    }

    record OrderItemDto(
            UUID id,
            UUID productId,
            Integer quantity,
            BigDecimal priceAtTimeOfPurchase) {
    }

    record PaymentConfirmRequest(
            String paymentMethod,
            String transactionId) {
    }
}
