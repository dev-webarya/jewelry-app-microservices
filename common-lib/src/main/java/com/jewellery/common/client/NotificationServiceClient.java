package com.jewellery.common.client;

import com.jewellery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Feign client for Notification Service
 * Used by Order, User, and Payment services
 */
@FeignClient(name = "notification-service", path = "/api/v1/notifications")
public interface NotificationServiceClient {

    @PostMapping("/email")
    ApiResponse<Void> sendEmail(@RequestBody EmailNotificationRequest request);

    @PostMapping("/otp")
    ApiResponse<Void> sendOtp(@RequestBody OtpNotificationRequest request);

    @PostMapping("/order-confirmation")
    ApiResponse<Void> sendOrderConfirmation(@RequestBody OrderNotificationRequest request);

    record EmailNotificationRequest(
            String to,
            String subject,
            String body) {
    }

    record OtpNotificationRequest(
            String to,
            String otp) {
    }

    record OrderNotificationRequest(
            String to,
            String orderNumber,
            String status,
            String message) {
    }
}
