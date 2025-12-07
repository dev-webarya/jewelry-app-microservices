package com.jewellery.orderservice.client;

import com.jewellery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "product-service", path = "/api/v1/products")
public interface ProductClient {
    @GetMapping("/{id}")
    ApiResponse<ProductDto> getProductById(@PathVariable UUID id);

    record ProductDto(UUID id, String sku, String name, BigDecimal basePrice, boolean isActive) {
    }
}
