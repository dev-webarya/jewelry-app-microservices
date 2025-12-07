package com.jewellery.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StockReservationRequest {
    @NotNull
    private UUID productId;

    private UUID storeId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
