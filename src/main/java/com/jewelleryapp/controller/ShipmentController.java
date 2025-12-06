package com.jewelleryapp.controller;

import com.jewelleryapp.dto.request.ShipmentRequestDto;
import com.jewelleryapp.dto.response.ShipmentResponseDto;
import com.jewelleryapp.enums.ShipmentStatus;
import com.jewelleryapp.service.ShipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

    private final ShipmentService shipmentService;

    // Only Admin/Manager can create shipments (generate labels)
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STORE_MANAGER')")
    public ResponseEntity<ShipmentResponseDto> createShipment(@Valid @RequestBody ShipmentRequestDto request) {
        return new ResponseEntity<>(shipmentService.createShipment(request), HttpStatus.CREATED);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_STORE_MANAGER')")
    public ResponseEntity<ShipmentResponseDto> updateStatus(@PathVariable UUID id, @RequestParam ShipmentStatus status) {
        return ResponseEntity.ok(shipmentService.updateShipmentStatus(id, status));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ShipmentResponseDto> getShipmentByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(shipmentService.getShipmentByOrderId(orderId));
    }
}