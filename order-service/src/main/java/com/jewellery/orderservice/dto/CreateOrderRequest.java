package com.jewellery.orderservice.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
public class CreateOrderRequest {
    @NotNull(message = "User ID is required")
    private UUID userId;

    private UUID fulfillmentStoreId;

    @NotEmpty(message = "At least one item is required")
    private List<OrderItemRequest> items;

    private String shippingAddress;
    private String billingAddress;

    @Data
    public static class OrderItemRequest {
        @NotNull(message = "Product ID is required")
        private UUID productId;

        @NotNull(message = "Quantity is required")
        private Integer quantity;

        @NotNull(message = "Price is required")
        private BigDecimal priceAtTimeOfPurchase;
    }
}
