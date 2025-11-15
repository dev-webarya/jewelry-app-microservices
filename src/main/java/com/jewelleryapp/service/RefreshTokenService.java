package com.jewelleryapp.service;

import com.jewelleryapp.entity.User;
import com.jewelleryapp.model.RefreshToken;
import com.jewelleryapp.repository.RefreshTokenRepository;
import com.jewelleryapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.jewelleryapp.exception.InvalidRequestException;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshExpiration;

    @Transactional
    public RefreshToken createRefreshToken(String email) { // Changed username to email
        User user = userRepository.findByEmail(email) // Changed findByUsername to findByEmail
                .orElseThrow(() -> new RuntimeException("User not found"));

        return refreshTokenRepository.findByUser(user)
                .map(token -> {
                    token.setExpiryDate(Instant.now().plusMillis(refreshExpiration));
                    token.setToken(UUID.randomUUID().toString());
                    return token;
                })
                .orElseGet(() -> {
                    RefreshToken newToken = RefreshToken.builder()
                            .user(user)
                            .token(UUID.randomUUID().toString())
                            .expiryDate(Instant.now().plusMillis(refreshExpiration))
                            .build();
                    return refreshTokenRepository.save(newToken);
                });
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new InvalidRequestException(token.getToken() + " Refresh token was expired. Please make a new signin request");
        }
        return token;
    }

    @Transactional
    public void deleteByUserId(UUID userId) { // Changed from Long to UUID
        userRepository.findById(userId).ifPresent(refreshTokenRepository::deleteByUser);
    }

    @Transactional
    public void deleteByUsername(String email) { // Changed username to email
        userRepository.findByEmail(email).ifPresent(refreshTokenRepository::deleteByUser); // Changed findByUsername to findByEmail
    }
}