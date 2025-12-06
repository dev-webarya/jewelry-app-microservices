package com.jewelleryapp.config;

import com.jewelleryapp.service.notification.NotificationSender;
import com.jewelleryapp.service.notification.impl.NotificationApiSender;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class NotificationConfig {

    @Value("${notification.client.id}")
    private String clientId;

    @Value("${notification.client.secret}")
    private String clientSecret;

    @PostConstruct
    public void logConfig() {
        log.info("--- Initializing Notification Service ---");

        if (clientId == null || clientId.isEmpty() || clientId.contains("$")) {
            log.error("CRITICAL: Notification Client ID is missing or failed to resolve! Current value: '{}'. Check application.properties and Docker Environment Variables.", clientId);
        } else {
            // Log the first few chars to verify it's the correct key (hvsy...) without leaking the whole thing
            String maskedId = clientId.length() > 4 ? clientId.substring(0, 4) + "***" : "***";
            log.info("Notification Client ID loaded successfully: {}", maskedId);
        }

        if (clientSecret == null || clientSecret.isEmpty()) {
            log.error("CRITICAL: Notification Client Secret is missing!");
        } else {
            log.info("Notification Client Secret loaded successfully.");
        }
    }

    @Bean
    public NotificationSender notificationSender() {
        // Instantiate the implementation manually using the injected values
        return new NotificationApiSender(clientId, clientSecret);
    }
}