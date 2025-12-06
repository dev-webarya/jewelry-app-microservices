package com.jewelleryapp.service.notification;

import com.jewelleryapp.service.notification.model.NotificationMessage;

/**
 * Interface to decouple the external notification library from business logic.
 */
public interface NotificationSender {
    void send(String email, String phoneNumber, NotificationMessage message);
}