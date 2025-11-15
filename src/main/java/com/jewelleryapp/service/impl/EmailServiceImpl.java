package com.jewelleryapp.service.impl;

import com.jewelleryapp.exception.EmailSendingException;
import com.jewelleryapp.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendOtpEmail(String to, String otp) {
        log.info("Attempting to send OTP email to: {}", to);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Verify Your Account - Jewellery App");
            message.setText("Your One-Time Password (OTP) is: " + otp + "\n\nThis OTP is valid for 10 minutes.");

            mailSender.send(message);

            log.info("Verification email sent successfully to {}", to);

        } catch (MailException e) {
            log.error("Failed to send email to {}. Error: {}", to, e.getMessage(), e);
            throw new EmailSendingException("Failed to send verification email. Please try again later.", e);
        }
    }
}