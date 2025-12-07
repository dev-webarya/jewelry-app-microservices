package com.jewellery.common.client;

import com.jewellery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Feign client for Product Service
 * Used by Order, Cart, and Inventory services
 */
@FeignClient(name = "product-service", path = "/api/v1/products")
public interface ProductServiceClient {

    @GetMapping("/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable UUID id);

    @GetMapping("/by-ids")
    ApiResponse<List<ProductDto>> getProductsByIds(@PathVariable List<UUID> ids);

    /**
     * Minimal Product DTO for inter-service communication
     */
    record ProductDto(
            UUID id,
            String sku,
            String name,
            String description,
            BigDecimal basePrice,
            boolean isActive,
            UUID categoryId,
            String categoryName) {
    }
}
