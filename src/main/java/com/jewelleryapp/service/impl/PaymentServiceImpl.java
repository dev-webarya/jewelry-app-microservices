package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.PaymentInitiateRequestDto;
import com.jewelleryapp.dto.response.PaymentResponseDto;
import com.jewelleryapp.entity.CustomerOrder;
import com.jewelleryapp.entity.Payment;
import com.jewelleryapp.enums.OrderStatus;
import com.jewelleryapp.enums.PaymentStatus;
import com.jewelleryapp.exception.InvalidRequestException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.PaymentMapper;
import com.jewelleryapp.repository.OrderRepository;
import com.jewelleryapp.repository.PaymentRepository;
import com.jewelleryapp.service.OrderService;
import com.jewelleryapp.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final PaymentMapper paymentMapper;
    private final OrderService orderService; // To update order status

    @Override
    @Transactional
    public PaymentResponseDto initiatePayment(PaymentInitiateRequestDto request) {
        CustomerOrder order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", request.getOrderId()));

        // Validation: Cannot pay for cancelled or already paid orders
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new InvalidRequestException("Order is not in a valid state for payment.");
        }

        // --- 1. MOCK RAZORPAY ORDER CREATION ---
        String transactionId = "pay_" + UUID.randomUUID().toString().substring(0, 8); // Mock ID
        log.info("Mocking Razorpay Order Creation. Transaction ID: {}", transactionId);
        // ----------------------------------------

        Payment payment = Payment.builder()
                .order(order)
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .status(PaymentStatus.PENDING) // Initially Pending
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // For simulation purposes, we will auto-verify this payment immediately
        return verifyPayment(transactionId, "SUCCESS");
    }

    @Override
    @Transactional
    public PaymentResponseDto verifyPayment(String transactionId, String statusStr) {
        Payment payment = paymentRepository.findByTransactionId(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "transactionId", transactionId));

        PaymentStatus newStatus = PaymentStatus.valueOf(statusStr);
        payment.setStatus(newStatus);

        Payment savedPayment = paymentRepository.save(payment);

        if (newStatus == PaymentStatus.SUCCESS) {
            // Automatically move Order to PROCESSING (which deducts stock!)
            // --- FIX: Use the dedicated confirmation method to bypass Admin checks ---
            orderService.confirmOrderPayment(payment.getOrder().getId(), payment.getPaymentMethod());
            log.info("Payment Success. Order {} moved to PROCESSING.", payment.getOrder().getOrderNumber());
        } else if (newStatus == PaymentStatus.FAILED) {
            log.warn("Payment Failed for Order {}", payment.getOrder().getOrderNumber());
        }

        return paymentMapper.toDto(savedPayment);
    }

    @Override
    public PaymentResponseDto getPaymentByOrderId(UUID orderId) {
        List<Payment> payments = paymentRepository.findByOrderId(orderId);
        if (payments.isEmpty()) {
            throw new ResourceNotFoundException("No payment records found for this order.");
        }
        // Return the latest one
        return paymentMapper.toDto(payments.get(payments.size() - 1));
    }
}