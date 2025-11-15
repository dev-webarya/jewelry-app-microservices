package com.jewelleryapp.controller;

import com.jewelleryapp.dto.request.*;
import com.jewelleryapp.dto.response.AuthenticationResponse;
import com.jewelleryapp.dto.response.RegistrationResponse;
import com.jewelleryapp.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }
    
    /**
     * Development-only endpoint to register a user and immediately enable them,
     * bypassing the OTP verification step.
     */
    @PostMapping("/dev-register")
    public ResponseEntity<AuthenticationResponse> devRegister(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authenticationService.devRegister(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authenticationService.verifyEmailOtp(request));
    }

    @PostMapping("/request-otp")
    public ResponseEntity<RegistrationResponse> requestOtp(@Valid @RequestBody OtpRequest request) {
        return ResponseEntity.ok(authenticationService.requestOtp(request));
    }

    @PostMapping("/verify-login-otp")
    public ResponseEntity<AuthenticationResponse> verifyLoginOtp(@Valid @RequestBody LoginOtpRequest request) {
        return ResponseEntity.ok(authenticationService.verifyLoginOtp(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<RegistrationResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        return ResponseEntity.ok(authenticationService.resetPassword(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authenticationService.refreshToken(request));
    }
}