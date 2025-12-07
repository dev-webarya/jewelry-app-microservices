package com.jewellery.common.client;

import com.jewellery.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

/**
 * Feign client for User Service
 * Used by other services to validate users and get user information
 */
@FeignClient(name = "user-service", path = "/api/v1/users")
public interface UserServiceClient {

    @GetMapping("/{id}")
    ApiResponse<UserDto> getUserById(@PathVariable UUID id);

    @GetMapping("/by-email")
    ApiResponse<UserDto> getUserByEmail(@RequestParam String email);

    /**
     * Minimal User DTO for inter-service communication
     */
    record UserDto(
            UUID id,
            String firstName,
            String lastName,
            String email,
            String phoneNumber,
            boolean isEnabled) {
    }
}
