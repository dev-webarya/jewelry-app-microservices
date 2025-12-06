package com.jewelleryapp.enums;

public enum OrderStatus {
    PAYMENT_PENDING,
    PROCESSING,       // Trigger for Stock Deduction
    SHIPPED,
    DELIVERED,
    CANCELLED,        // Trigger for Stock Restoration (Optional enhancement)
    RETURN_REQUESTED,
    RETURNED
}