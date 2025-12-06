package com.jewelleryapp.service.notification.model;

/**
 * Modern Java Record for immutable data transfer.
 */
public record NotificationMessage(
        String templateId,
        String emailSubject,
        String emailHtmlBody,
        String smsMessage
) {}