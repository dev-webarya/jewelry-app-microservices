package com.jewellery.inventoryservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class StockItemRequestDto {
    @NotNull(message = "Product ID is required")
    private UUID productId;

    private UUID storeId; // null = central warehouse

    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity must be non-negative")
    private Integer quantity;
}
