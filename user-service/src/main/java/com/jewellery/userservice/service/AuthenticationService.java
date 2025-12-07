package com.jewellery.userservice.service;

import com.jewellery.common.exception.DuplicateResourceException;
import com.jewellery.common.exception.InvalidRequestException;
import com.jewellery.common.exception.ResourceNotFoundException;
import com.jewellery.userservice.dto.*;
import com.jewellery.userservice.entity.EmailOtp;
import com.jewellery.userservice.entity.RefreshToken;
import com.jewellery.userservice.entity.Role;
import com.jewellery.userservice.entity.User;
import com.jewellery.userservice.repository.EmailOtpRepository;
import com.jewellery.userservice.repository.RefreshTokenRepository;
import com.jewellery.userservice.repository.RoleRepository;
import com.jewellery.userservice.repository.UserRepository;
import com.jewellery.userservice.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final EmailOtpRepository emailOtpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Value("${application.security.jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    private static final String DEFAULT_ROLE = "ROLE_CUSTOMER";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists.");
        }

        if (request.getPhoneNumber() != null
                && userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException(
                    "User with phone number '" + request.getPhoneNumber() + "' already exists.");
        }

        Role defaultRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new InvalidRequestException("Default role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        // For dev: auto-assign admin role if email contains "+admin@"
        if (request.getEmail().contains("+admin@")) {
            roleRepository.findByName(ADMIN_ROLE).ifPresent(roles::add);
        }

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .isEnabled(true) // Auto-enabled for dev
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthenticationResponse(savedUser);
    }

    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (DisabledException e) {
            throw new DisabledException("Account not verified.");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return buildAuthenticationResponse(user);
    }

    @Transactional
    public AuthenticationResponse refreshToken(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new InvalidRequestException("Invalid refresh token"));

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new InvalidRequestException("Refresh token has expired");
        }

        User user = token.getUser();
        if (!user.isEnabled()) {
            throw new InvalidRequestException("User account is not enabled");
        }

        String accessToken = jwtService.generateToken(user);
        long expiresAt = System.currentTimeMillis() + jwtService.getExpirationTime();

        return AuthenticationResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(expiresAt)
                .build();
    }

    private AuthenticationResponse buildAuthenticationResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = createRefreshToken(user);
        long expiresAt = System.currentTimeMillis() + jwtService.getExpirationTime();

        return AuthenticationResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .accessTokenExpiresAt(expiresAt)
                .build();
    }

    private RefreshToken createRefreshToken(User user) {
        // Delete existing refresh tokens for user
        refreshTokenRepository.deleteByUser(user);

        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        return refreshTokenRepository.save(refreshToken);
    }
}
