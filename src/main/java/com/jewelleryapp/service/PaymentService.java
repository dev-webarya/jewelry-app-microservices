package com.jewelleryapp.service;

import com.jewelleryapp.dto.request.PaymentInitiateRequestDto;
import com.jewelleryapp.dto.response.PaymentResponseDto;

import java.util.UUID;

public interface PaymentService {

    // 1. Initiate Payment (Creates order in Gateway)
    PaymentResponseDto initiatePayment(PaymentInitiateRequestDto request);

    // 2. Verify Payment (Webhook/Callback from Gateway)
    PaymentResponseDto verifyPayment(String transactionId, String paymentStatus);

    PaymentResponseDto getPaymentByOrderId(UUID orderId);
}