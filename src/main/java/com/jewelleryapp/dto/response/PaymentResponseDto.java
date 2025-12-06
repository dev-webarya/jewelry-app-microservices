package com.jewelleryapp.dto.response;

import com.jewelleryapp.enums.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class PaymentResponseDto {
    private UUID id;
    private UUID orderId;
    private String transactionId;
    private BigDecimal amount;
    private String paymentMethod;
    private PaymentStatus status;
    private LocalDateTime processedAt;

    // For Razorpay Frontend Integration
    private String razorpayOrderId;
    private String keyId;
}