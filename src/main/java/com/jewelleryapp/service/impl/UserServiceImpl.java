package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.UserRequest;
import com.jewelleryapp.dto.response.UserResponse;
import com.jewelleryapp.entity.Role;
import com.jewelleryapp.entity.User;
import com.jewelleryapp.exception.DuplicateResourceException;
import com.jewelleryapp.exception.InvalidRequestException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.UserMapper;
import com.jewelleryapp.repository.RoleRepository;
import com.jewelleryapp.repository.UserRepository;
import com.jewelleryapp.service.UserService;
import com.jewelleryapp.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository; // Injected
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserSpecification userSpecification;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.getEmail())) { // Changed
            throw new DuplicateResourceException("User with email '" + userRequest.getEmail() + "' already exists.");
        }
        if (userRequest.getPhoneNumber() != null && userRepository.findByPhoneNumber(userRequest.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("User with phone number '" + userRequest.getPhoneNumber() + "' already exists.");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setPhoneNumber(userRequest.getPhoneNumber()); // Set phone number

        // Admins creating users can set roles
        if (userRequest.getRoles() == null || userRequest.getRoles().isEmpty()) {
            user.getRoles().add(findRoleByName("ROLE_CUSTOMER")); // Default role
        } else {
            Set<Role> roles = userRequest.getRoles().stream()
                    .map(this::findRoleByName)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        // Admin-created users are enabled by default
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable, String searchTerm) {
        Specification<User> spec = userSpecification.searchByTerm(searchTerm);
        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) { // Changed from Long
        User user = findUserById(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserRequest userRequest) { // Changed from Long
        User existingUser = findUserById(userId);

        // Check email uniqueness if changed
        if (userRequest.getEmail() != null && !userRequest.getEmail().equals(existingUser.getEmail())) {
            userRepository.findByEmail(userRequest.getEmail()).ifPresent(u -> {
                throw new DuplicateResourceException("Another user with email '" + userRequest.getEmail() + "' already exists.");
            });
            existingUser.setEmail(userRequest.getEmail()); // Update email
        }

        // Check phone number uniqueness if changed
        if (userRequest.getPhoneNumber() != null && !userRequest.getPhoneNumber().equals(existingUser.getPhoneNumber())) {
            userRepository.findByPhoneNumber(userRequest.getPhoneNumber()).ifPresent(u -> {
                throw new DuplicateResourceException("Another user with phone number '" + userRequest.getPhoneNumber() + "' already exists.");
            });
            existingUser.setPhoneNumber(userRequest.getPhoneNumber()); // Update phone
        }

        // Update basic fields
        userMapper.updateEntityFromRequest(userRequest, existingUser);

        // Update password if provided
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        // Update roles if provided
        if (userRequest.getRoles() != null) {
            Set<Role> roles = userRequest.getRoles().stream()
                    .map(this::findRoleByName)
                    .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }

        User updatedUser = userRepository.save(existingUser);
        return userMapper.toResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) { // Changed from Long
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private User findUserById(UUID userId) { // Changed from Long
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    // Helper to find roles
    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new InvalidRequestException("Role not found: " + roleName + ". Please ensure roles (e.g., ROLE_CUSTOMER, ROLE_ADMIN) exist in the database."));
    }
}