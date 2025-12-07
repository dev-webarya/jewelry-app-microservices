package com.jewellery.orderservice.dto;

import com.jewellery.orderservice.enums.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private UUID id;
    private String orderNumber;
    private UUID userId;
    private UUID fulfillmentStoreId;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private String billingAddress;
    private List<OrderItemDto> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private Integer quantity;
        private BigDecimal priceAtTimeOfPurchase;
    }
}
