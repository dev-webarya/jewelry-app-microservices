package com.jewelleryapp.dto.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@Data
public class CartResponseDto {
    private UUID id;
    private Set<CartItemDto> items;
    private BigDecimal totalPrice;
    private Integer totalItems;

    @Data
    public static class CartItemDto {
        private UUID id;
        private UUID productId;
        private String productName;
        private String productImageUrl; // Handy for UI
        private BigDecimal unitPrice;
        private Integer quantity;
        private BigDecimal subTotal;
    }
}