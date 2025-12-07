package com.jewellery.userservice.repository;

import com.jewellery.userservice.entity.EmailOtp;
import com.jewellery.userservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailOtpRepository extends JpaRepository<EmailOtp, UUID> {
    Optional<EmailOtp> findByUser(User user);
}
