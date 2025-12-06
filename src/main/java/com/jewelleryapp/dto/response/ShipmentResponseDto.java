package com.jewelleryapp.dto.response;

import com.jewelleryapp.enums.ShipmentStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ShipmentResponseDto {
    private UUID id;
    private UUID orderId;
    private String trackingNumber;
    private String carrier;
    private ShipmentStatus status;
    private LocalDateTime createdAt;
}