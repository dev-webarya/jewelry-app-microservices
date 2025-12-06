package com.jewelleryapp.service;

import com.jewelleryapp.dto.request.UserRequest;
import com.jewelleryapp.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    UserResponse createUser(UserRequest userRequest);
    Page<UserResponse> getAllUsers(Pageable pageable, String searchTerm);
    UserResponse getUserById(UUID userId);
    UserResponse updateUser(UUID userId, UserRequest userRequest);

    // --- New Self-Management Methods ---
    UserResponse getCurrentUser();
    UserResponse updateCurrentUser(UserRequest userRequest);

    void deleteUser(UUID userId);
}