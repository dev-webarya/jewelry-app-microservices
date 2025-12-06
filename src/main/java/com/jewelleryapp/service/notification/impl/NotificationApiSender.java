package com.jewelleryapp.service.notification.impl;

import com.jewelleryapp.service.notification.NotificationSender;
import com.jewelleryapp.service.notification.model.NotificationMessage;
import com.notificationapi.NotificationApi;
import com.notificationapi.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationApiSender implements NotificationSender {

    private final NotificationApi api;

    public NotificationApiSender(
            @Value("${notification.client.id}") String clientId,
            @Value("${notification.client.secret}") String clientSecret
    ) {
        this.api = new NotificationApi(clientId, clientSecret);
    }

    @Override
    @Async // Make sending asynchronous to not block the order flow
    public void send(String email, String phoneNumber, NotificationMessage message) {
        try {
            // Ensure phone number has country code or fallback (Library requirement)
            String validPhone = (phoneNumber != null && !phoneNumber.isBlank()) ? phoneNumber : "+15550000000";

            User user = new User(email)
                    .setEmail(email)
                    .setNumber(validPhone);

            EmailOptions emailOptions = new EmailOptions()
                    .setSubject(message.emailSubject())
                    .setHtml(message.emailHtmlBody());

            SmsOptions smsOptions = new SmsOptions()
                    .setMessage(message.smsMessage());

            NotificationRequest request = new NotificationRequest(message.templateId(), user)
                    .setEmail(emailOptions)
                    .setSms(smsOptions);

            log.info("Sending notification [{}] to {}", message.templateId(), email);
            api.send(request);

        } catch (Exception e) {
            // Log error but do not throw exception to avoid rolling back business transactions
            log.error("Failed to send notification to {}: {}", email, e.getMessage());
        }
    }
}