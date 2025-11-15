package com.jewelleryapp.service;

import com.jewelleryapp.dto.request.*;
import com.jewelleryapp.dto.response.AuthenticationResponse;
import com.jewelleryapp.dto.response.RegistrationResponse;

public interface AuthenticationService {
    RegistrationResponse register(RegisterRequest request);
    AuthenticationResponse devRegister(RegisterRequest request);
    AuthenticationResponse login(LoginRequest request);
    AuthenticationResponse refreshToken(RefreshTokenRequest request);
    AuthenticationResponse verifyEmailOtp(VerifyOtpRequest request);
    RegistrationResponse requestOtp(OtpRequest request);
    AuthenticationResponse verifyLoginOtp(LoginOtpRequest request);
    RegistrationResponse resetPassword(PasswordResetRequest request);
}