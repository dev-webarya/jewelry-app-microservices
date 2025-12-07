package com.jewellery.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private UUID id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal basePrice;
    private boolean isActive;
    private UUID categoryId;
    private String categoryName;
    private List<String> collections;
    private List<ProductImageDto> images;
    private Instant createdAt;
    private Instant updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductImageDto {
        private UUID id;
        private String imageUrl;
        private boolean isPrimary;
        private Integer displayOrder;
    }
}
