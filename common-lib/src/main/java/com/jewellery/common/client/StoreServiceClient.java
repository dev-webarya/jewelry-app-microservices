package com.jewellery.common.client;

import com.jewellery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Feign client for Store Service
 * Used by Order and Inventory services
 */
@FeignClient(name = "store-service", path = "/api/v1/stores")
public interface StoreServiceClient {

    @GetMapping("/{id}")
    ApiResponse<StoreDto> getStoreById(@PathVariable UUID id);

    record StoreDto(
            UUID id,
            String name,
            String address,
            BigDecimal latitude,
            BigDecimal longitude,
            String operatingHours,
            String contactPhone) {
    }
}
