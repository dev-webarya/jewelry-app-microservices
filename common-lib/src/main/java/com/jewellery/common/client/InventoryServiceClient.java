package com.jewellery.common.client;

import com.jewellery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Feign client for Inventory Service
 * Used by Order service for stock validation and updates
 */
@FeignClient(name = "inventory-service", path = "/api/v1/stock-items")
public interface InventoryServiceClient {

    @GetMapping("/product/{productId}")
    ApiResponse<StockDto> getStockByProductId(@PathVariable UUID productId);

    @GetMapping("/product/{productId}/store/{storeId}")
    ApiResponse<StockDto> getStockByProductAndStore(
            @PathVariable UUID productId,
            @PathVariable UUID storeId);

    @PostMapping("/reserve")
    ApiResponse<Boolean> reserveStock(@RequestBody StockReservationRequest request);

    @PostMapping("/release")
    ApiResponse<Boolean> releaseStock(@RequestBody StockReservationRequest request);

    record StockDto(
            UUID id,
            UUID productId,
            UUID storeId,
            Integer quantity) {
    }

    record StockReservationRequest(
            UUID productId,
            UUID storeId,
            Integer quantity) {
    }
}
