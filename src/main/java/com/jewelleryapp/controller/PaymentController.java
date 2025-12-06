package com.jewelleryapp.controller;

import com.jewelleryapp.dto.request.PaymentInitiateRequestDto;
import com.jewelleryapp.dto.response.PaymentResponseDto;
import com.jewelleryapp.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    public ResponseEntity<PaymentResponseDto> initiatePayment(@Valid @RequestBody PaymentInitiateRequestDto request) {
        return ResponseEntity.ok(paymentService.initiatePayment(request));
    }

    // This endpoint mimics what Razorpay Webhook would call
    @PostMapping("/webhook/verify")
    public ResponseEntity<PaymentResponseDto> verifyPayment(@RequestParam String transactionId, @RequestParam String status) {
        return ResponseEntity.ok(paymentService.verifyPayment(transactionId, status));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponseDto> getPaymentByOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderId(orderId));
    }
}