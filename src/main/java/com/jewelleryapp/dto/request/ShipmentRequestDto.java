package com.jewelleryapp.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ShipmentRequestDto {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Carrier name is required (e.g., FedEx, BlueDart)")
    private String carrier;

    // Optional: If you have a tracking number already.
    // If null, service might generate a mock one.
    private String trackingNumber;
}