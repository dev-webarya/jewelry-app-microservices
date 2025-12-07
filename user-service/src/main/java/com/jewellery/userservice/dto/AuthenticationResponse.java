package com.jewellery.userservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private Set<String> roles;
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresAt;
}
