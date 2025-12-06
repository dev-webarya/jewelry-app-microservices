package com.jewelleryapp.service.notification;

import com.jewelleryapp.entity.CustomerOrder;
import com.jewelleryapp.entity.User;
import com.jewelleryapp.service.notification.model.NotificationMessage;
import com.jewelleryapp.service.notification.template.EmailTemplateEngine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

/**
 * High-level service handling business events.
 */
@Service
@RequiredArgsConstructor
public class JewelleryNotificationService {

    private final NotificationSender notificationSender;
    private final EmailTemplateEngine templateEngine;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    // --- Assets ---
    private static final String ICON_GEM = "\uD83D\uDC8E";      // 💎
    private static final String ICON_CHECK = "\u2705";          // ✅
    private static final String ICON_CROSS = "\u274C";          // ❌
    private static final String ICON_TRUCK = "\uD83D\uDE9A";    // 🚚

    private static final String COLOR_GOLD = "#D4AF37";
    private static final String COLOR_SUCCESS = "#10B981";
    private static final String COLOR_DANGER =  "#EF4444";
    private static final String COLOR_DARK =    "#1F2937";

    public void sendOtp(User user, String otp) {
        String emailHtml = templateEngine.buildProfessionalEmail(
                ICON_GEM, COLOR_GOLD,
                "Verify Account",
                "Use the code below to verify your identity.",
                user.getFirstName(),
                "OTP-SECURE",
                new String[][]{{"OTP Code", otp}, {"Validity", "10 Minutes"}},
                "Security Alert", "Do not share this code with anyone."
        );

        String sms = String.format("%s Your JewelleryApp OTP is %s. Valid for 10 mins.", ICON_GEM, otp);

        notificationSender.send(user.getEmail(), user.getPhoneNumber(),
                new NotificationMessage("auth_otp", "Verify Your Account", emailHtml, sms));
    }

    public void sendOrderConfirmation(CustomerOrder order) {
        String emailHtml = templateEngine.buildProfessionalEmail(
                ICON_CHECK, COLOR_SUCCESS,
                "Order Confirmed",
                "Thank you for shopping with us! We are getting your order ready.",
                order.getUser().getFirstName(),
                order.getOrderNumber(),
                new String[][]{
                        {"Amount", "$" + order.getTotalPrice()},
                        {"Items", String.valueOf(order.getItems().size())},
                        {"Date", order.getCreatedAt().format(dateFormatter)}
                },
                "Processing", "We will notify you when it ships."
        );

        String sms = String.format("%s Order %s Confirmed! Amount: $%s. We are processing it now.",
                ICON_CHECK, order.getOrderNumber(), order.getTotalPrice());

        notificationSender.send(order.getUser().getEmail(), order.getUser().getPhoneNumber(),
                new NotificationMessage("order_confirmed", "Order Confirmed - " + order.getOrderNumber(), emailHtml, sms));
    }

    public void sendOrderShipped(CustomerOrder order, String trackingNumber, String carrier) {
        String emailHtml = templateEngine.buildProfessionalEmail(
                ICON_TRUCK, COLOR_DARK,
                "Order Shipped",
                "Your sparkles are on the way!",
                order.getUser().getFirstName(),
                order.getOrderNumber(),
                new String[][]{
                        {"Carrier", carrier},
                        {"Tracking #", trackingNumber},
                        {"Destination", order.getShippingAddress()}
                },
                "In Transit", "Track your package on the carrier's website."
        );

        String sms = String.format("%s Order %s Shipped via %s. Track: %s",
                ICON_TRUCK, order.getOrderNumber(), carrier, trackingNumber);

        notificationSender.send(order.getUser().getEmail(), order.getUser().getPhoneNumber(),
                new NotificationMessage("order_shipped", "Order Shipped - " + order.getOrderNumber(), emailHtml, sms));
    }

    public void sendOrderCancelled(CustomerOrder order, String reason) {
        String emailHtml = templateEngine.buildProfessionalEmail(
                ICON_CROSS, COLOR_DANGER,
                "Order Cancelled",
                "Your order has been cancelled.",
                order.getUser().getFirstName(),
                order.getOrderNumber(),
                new String[][]{
                        {"Reason", reason},
                        {"Refund", "Initiated (if applicable)"}
                },
                "Refund Info", "Refunds typically process within 3-5 business days."
        );

        String sms = String.format("%s Order %s Cancelled. Reason: %s.",
                ICON_CROSS, order.getOrderNumber(), reason);

        notificationSender.send(order.getUser().getEmail(), order.getUser().getPhoneNumber(),
                new NotificationMessage("order_cancelled", "Order Cancelled - " + order.getOrderNumber(), emailHtml, sms));
    }
}