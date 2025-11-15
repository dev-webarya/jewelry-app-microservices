package com.jewelleryapp.repository;

import com.jewelleryapp.entity.EmailOtp;
import com.jewelleryapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findByUser(User user);

    Optional<EmailOtp> findByOtpAndUser_Email(String otp, String email); // Changed from User_Username
}