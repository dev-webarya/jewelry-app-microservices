package com.jewellery.orderservice.client;

import com.jewellery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@FeignClient(name = "inventory-service", path = "/api/v1/stock-items")
public interface InventoryClient {

    @PostMapping("/reserve")
    ApiResponse<Boolean> reserveStock(@RequestBody StockReservationRequest request);

    @PostMapping("/release")
    ApiResponse<Boolean> releaseStock(@RequestBody StockReservationRequest request);

    record StockReservationRequest(UUID productId, UUID storeId, Integer quantity) {
    }
}
