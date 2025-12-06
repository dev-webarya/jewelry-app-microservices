package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.ShipmentRequestDto;
import com.jewelleryapp.dto.response.ShipmentResponseDto;
import com.jewelleryapp.entity.CustomerOrder;
import com.jewelleryapp.entity.Shipment;
import com.jewelleryapp.enums.OrderStatus;
import com.jewelleryapp.enums.ShipmentStatus;
import com.jewelleryapp.exception.InvalidRequestException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.ShipmentMapper;
import com.jewelleryapp.repository.OrderRepository;
import com.jewelleryapp.repository.ShipmentRepository;
import com.jewelleryapp.service.notification.JewelleryNotificationService;
import com.jewelleryapp.service.OrderService;
import com.jewelleryapp.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final OrderRepository orderRepository;
    private final ShipmentMapper shipmentMapper;
    private final OrderService orderService;

    // --- INTEGRATION: Notification Service ---
    private final JewelleryNotificationService notificationService;

    @Override
    @Transactional
    public ShipmentResponseDto createShipment(ShipmentRequestDto request) {
        CustomerOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        if (order.getStatus() != OrderStatus.PROCESSING) {
            throw new InvalidRequestException("Order cannot be shipped. Current status: " + order.getStatus());
        }

        String trackingNum = (request.getTrackingNumber() != null)
                ? request.getTrackingNumber()
                : "TRK" + System.currentTimeMillis();

        Shipment shipment = Shipment.builder()
                .order(order)
                .carrier(request.getCarrier())
                .trackingNumber(trackingNum)
                .status(ShipmentStatus.PREPARING)
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);

        orderService.updateOrderStatus(order.getId(), OrderStatus.SHIPPED,
                "Shipped via " + request.getCarrier() + ". Tracking: " + trackingNum);

        // --- NOTIFICATION: Order Shipped ---
        notificationService.sendOrderShipped(order, trackingNum, request.getCarrier());

        return shipmentMapper.toDto(savedShipment);
    }

    @Override
    @Transactional
    public ShipmentResponseDto updateShipmentStatus(UUID shipmentId, ShipmentStatus newStatus) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", shipmentId));

        shipment.setStatus(newStatus);

        if (newStatus == ShipmentStatus.DELIVERED) {
            orderService.updateOrderStatus(shipment.getOrder().getId(), OrderStatus.DELIVERED, "Package marked delivered by carrier.");
        }

        return shipmentMapper.toDto(shipmentRepository.save(shipment));
    }

    @Override
    public ShipmentResponseDto getShipmentByOrderId(UUID orderId) {
        List<Shipment> shipments = shipmentRepository.findByOrderId(orderId);
        if (shipments.isEmpty()) {
            throw new ResourceNotFoundException("No shipment info found for this order.");
        }
        return shipmentMapper.toDto(shipments.get(0));
    }
}