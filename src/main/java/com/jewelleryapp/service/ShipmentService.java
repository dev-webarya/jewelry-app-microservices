package com.jewelleryapp.service;

import com.jewelleryapp.dto.request.ShipmentRequestDto;
import com.jewelleryapp.dto.response.ShipmentResponseDto;
import com.jewelleryapp.enums.ShipmentStatus;

import java.util.UUID;

public interface ShipmentService {

    // Create a shipment label/record
    ShipmentResponseDto createShipment(ShipmentRequestDto request);

    // Update status (e.g., from Webhook or manual scan)
    ShipmentResponseDto updateShipmentStatus(UUID shipmentId, ShipmentStatus status);

    ShipmentResponseDto getShipmentByOrderId(UUID orderId);
}