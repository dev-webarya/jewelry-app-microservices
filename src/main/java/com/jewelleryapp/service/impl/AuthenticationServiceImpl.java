package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.*;
import com.jewelleryapp.dto.response.AuthenticationResponse;
import com.jewelleryapp.dto.response.RegistrationResponse;
import com.jewelleryapp.entity.EmailOtp;
import com.jewelleryapp.entity.Role;
import com.jewelleryapp.entity.User;
import com.jewelleryapp.exception.DuplicateResourceException;
import com.jewelleryapp.exception.InvalidRequestException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.model.RefreshToken;
import com.jewelleryapp.repository.EmailOtpRepository;
import com.jewelleryapp.repository.RoleRepository;
import com.jewelleryapp.repository.UserRepository;
import com.jewelleryapp.security.JwtService;
import com.jewelleryapp.service.AuthenticationService;
import com.jewelleryapp.service.notification.JewelleryNotificationService;
import com.jewelleryapp.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;
    private final EmailOtpRepository emailOtpRepository;

    // --- INTEGRATION: Notification Service ---
    private final JewelleryNotificationService notificationService;

    private static final String DEFAULT_ROLE = "ROLE_CUSTOMER";
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        var existingUserOpt = userRepository.findByEmail(request.getEmail());

        if (existingUserOpt.isPresent()) {
            User existingUser = existingUserOpt.get();
            if (existingUser.isEnabled()) {
                throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists.");
            } else {
                existingUser.setFirstName(request.getFirstName());
                existingUser.setLastName(request.getLastName());
                existingUser.setPhoneNumber(request.getPhoneNumber());
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                userRepository.save(existingUser);
                sendOtpEmail(existingUser);
                return new RegistrationResponse("Account already registered. A new verification OTP has been sent to your email.");
            }
        }

        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("User with phone number '" + request.getPhoneNumber() + "' already exists.");
        }

        Role defaultRole = findRoleByName(DEFAULT_ROLE);
        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .isEnabled(false)
                .build();
        User savedUser = userRepository.save(user);

        sendOtpEmail(savedUser);

        return new RegistrationResponse("Registration successful. Please check your email for the OTP.");
    }

    @Override
    @Transactional
    public AuthenticationResponse devRegister(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("User exists.");
        }
        if (userRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("Phone number exists.");
        }

        Set<Role> roles = new HashSet<>();
        if (request.getEmail().contains("+admin@")) {
            roles.add(findRoleByName(ADMIN_ROLE));
        } else {
            roles.add(findRoleByName(DEFAULT_ROLE));
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .isEnabled(true)
                .build();
        User savedUser = userRepository.save(user);

        return buildAuthenticationResponse(savedUser);
    }

    @Override
    @Transactional
    public AuthenticationResponse verifyEmailOtp(VerifyOtpRequest request) {
        EmailOtp emailOtp = findAndValidateOtp(request.getEmail(), request.getOtp());
        User user = emailOtp.getUser();

        if (user.isEnabled()) {
            throw new InvalidRequestException("Account is already verified.");
        }

        user.setEnabled(true);
        userRepository.save(user);
        emailOtpRepository.delete(emailOtp);

        return buildAuthenticationResponse(user);
    }

    @Override
    @Transactional
    public RegistrationResponse requestOtp(OtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));
        sendOtpEmail(user);
        return new RegistrationResponse("OTP has been sent to your email.");
    }

    @Override
    @Transactional
    public AuthenticationResponse verifyLoginOtp(LoginOtpRequest request) {
        EmailOtp emailOtp = findAndValidateOtp(request.getEmail(), request.getOtp());
        User user = emailOtp.getUser();
        if (!user.isEnabled()) throw new InvalidRequestException("Account is not verified.");
        emailOtpRepository.delete(emailOtp);
        return buildAuthenticationResponse(user);
    }

    @Override
    @Transactional
    public RegistrationResponse resetPassword(PasswordResetRequest request) {
        EmailOtp emailOtp = findAndValidateOtp(request.getEmail(), request.getOtp());
        User user = emailOtp.getUser();
        if (!user.isEnabled()) throw new InvalidRequestException("Account not verified.");
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        emailOtpRepository.delete(emailOtp);
        return new RegistrationResponse("Password has been reset successfully.");
    }

    @Override
    @Transactional
    public AuthenticationResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (DisabledException e) {
            User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
            sendOtpEmail(user);
            throw new DisabledException("Account not verified. New OTP sent.");
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found after authentication."));

        return buildAuthenticationResponse(user);
    }

    @Override
    public AuthenticationResponse refreshToken(RefreshTokenRequest request) {
        return refreshTokenService.findByToken(request.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    if (!user.isEnabled()) throw new InvalidRequestException("User account is not enabled.");
                    String accessToken = jwtService.generateToken(user);
                    long expiresAt = System.currentTimeMillis() + jwtService.getExpirationTime();
                    return AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.getToken())
                            .accessTokenExpiresAt(expiresAt)
                            .id(user.getId())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                            .build();
                })
                .orElseThrow(() -> new InvalidRequestException("Refresh token expired or invalid!"));
    }

    private void sendOtpEmail(User user) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        emailOtpRepository.findByUser(user).ifPresent(emailOtpRepository::delete);

        EmailOtp emailOtp = EmailOtp.builder()
                .user(user)
                .otp(otp)
                .expiryDate(Instant.now().plusSeconds(600))
                .build();
        emailOtpRepository.save(emailOtp);

        // --- NOTIFICATION: Send OTP ---
        notificationService.sendOtp(user, otp);
    }

    private AuthenticationResponse buildAuthenticationResponse(User user) {
        String accessToken = jwtService.generateToken(user);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());
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

    private EmailOtp findAndValidateOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        EmailOtp emailOtp = emailOtpRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("No OTP found."));

        if (emailOtp.getExpiryDate().isBefore(Instant.now())) {
            emailOtpRepository.delete(emailOtp);
            throw new InvalidRequestException("OTP has expired.");
        }

        if (!emailOtp.getOtp().equals(otp)) {
            throw new InvalidRequestException("Invalid OTP.");
        }

        return emailOtp;
    }

    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new InvalidRequestException("Role not found: " + roleName));
    }
}