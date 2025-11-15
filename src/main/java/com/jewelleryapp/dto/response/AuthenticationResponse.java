package com.jewelleryapp.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // Don't include null fields in the JSON response
public class AuthenticationResponse {
    private UUID id; // Changed from Long to UUID
    private String firstName;
    private String lastName;
    private Set<String> roles; // Changed from String role to Set<String> roles
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresAt;
}