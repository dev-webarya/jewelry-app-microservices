package com.jewelleryapp.service.impl;

import com.jewelleryapp.dto.request.UserRequest;
import com.jewelleryapp.dto.response.UserResponse;
import com.jewelleryapp.entity.Role;
import com.jewelleryapp.entity.Store;
import com.jewelleryapp.entity.User;
import com.jewelleryapp.exception.DuplicateResourceException;
import com.jewelleryapp.exception.InvalidRequestException;
import com.jewelleryapp.exception.ResourceNotFoundException;
import com.jewelleryapp.mapper.UserMapper;
import com.jewelleryapp.repository.RoleRepository;
import com.jewelleryapp.repository.StoreRepository;
import com.jewelleryapp.repository.UserRepository;
import com.jewelleryapp.service.UserService;
import com.jewelleryapp.specification.UserSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StoreRepository storeRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserSpecification userSpecification;

    @Override
    @Transactional
    public UserResponse createUser(UserRequest userRequest) {
        // 1. Identity Check
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // 2. Uniqueness Checks
        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new DuplicateResourceException("User with email '" + userRequest.getEmail() + "' already exists.");
        }
        if (userRequest.getPhoneNumber() != null && userRepository.findByPhoneNumber(userRequest.getPhoneNumber()).isPresent()) {
            throw new DuplicateResourceException("User with phone '" + userRequest.getPhoneNumber() + "' already exists.");
        }

        User user = userMapper.toEntity(userRequest);
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setPhoneNumber(userRequest.getPhoneNumber());

        // 3. Role Assignment Logic
        if (userRequest.getRoles() == null || userRequest.getRoles().isEmpty()) {
            user.getRoles().add(findRoleByName("ROLE_CUSTOMER"));
        } else {
            // Restriction: Only Admin can assign Admin/Manager roles
            boolean tryingToAssignPrivileged = userRequest.getRoles().stream()
                    .anyMatch(r -> r.contains("ADMIN") || r.contains("MANAGER"));

            if (tryingToAssignPrivileged && !isAdmin) {
                throw new AccessDeniedException("Only Admins can create Store Managers or other Admins.");
            }

            Set<Role> roles = userRequest.getRoles().stream()
                    .map(this::findRoleByName)
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }

        // 4. Store Assignment Logic (Admin Only)
        if (userRequest.getAssignedStoreId() != null) {
            if (!isAdmin) {
                throw new AccessDeniedException("Only Admins can assign users to stores.");
            }
            Store store = storeRepository.findById(userRequest.getAssignedStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", userRequest.getAssignedStoreId()));
            user.setManagedStore(store);
        }

        user.setEnabled(true); // Manually created users are auto-enabled
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable, String searchTerm) {
        Specification<User> spec = userSpecification.searchByTerm(searchTerm);
        return userRepository.findAll(spec, pageable).map(userMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID userId) {
        User user = findUserById(userId);
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(UUID userId, UserRequest userRequest) {
        User existingUser = findUserById(userId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        // Validation: Unique Email/Phone
        if (userRequest.getEmail() != null && !userRequest.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(userRequest.getEmail())) throw new DuplicateResourceException("Email exists");
            existingUser.setEmail(userRequest.getEmail());
        }

        userMapper.updateEntityFromRequest(userRequest, existingUser);

        // Update Password
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        // Update Roles (Admin Only)
        if (userRequest.getRoles() != null && isAdmin) {
            Set<Role> roles = userRequest.getRoles().stream()
                    .map(this::findRoleByName)
                    .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }

        // Update Store Assignment (Admin Only)
        if (userRequest.getAssignedStoreId() != null) {
            if (!isAdmin) throw new AccessDeniedException("Only Admins can change store assignments.");
            Store store = storeRepository.findById(userRequest.getAssignedStoreId())
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", userRequest.getAssignedStoreId()));
            existingUser.setManagedStore(store);
        }

        return userMapper.toResponse(userRepository.save(existingUser));
    }

    // --- NEW: Get Logged-in User Profile ---
    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        return userMapper.toResponse(currentUser);
    }

    // --- NEW: Update Logged-in User Profile ---
    @Override
    @Transactional
    public UserResponse updateCurrentUser(UserRequest userRequest) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = auth.getName();
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        // Safe Updates (Name, Phone, Password)
        if (userRequest.getFirstName() != null && !userRequest.getFirstName().isBlank()) {
            currentUser.setFirstName(userRequest.getFirstName());
        }

        if (userRequest.getLastName() != null && !userRequest.getLastName().isBlank()) {
            currentUser.setLastName(userRequest.getLastName());
        }

        // Phone Update with Uniqueness Check
        if (userRequest.getPhoneNumber() != null && !userRequest.getPhoneNumber().equals(currentUser.getPhoneNumber())) {
            userRepository.findByPhoneNumber(userRequest.getPhoneNumber()).ifPresent(u -> {
                throw new DuplicateResourceException("Phone number '" + userRequest.getPhoneNumber() + "' already exists.");
            });
            currentUser.setPhoneNumber(userRequest.getPhoneNumber());
        }

        // Password Update
        if (userRequest.getPassword() != null && !userRequest.getPassword().isBlank()) {
            currentUser.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        }

        // Roles and Store Assignments are deliberately IGNORED here for security.

        return userMapper.toResponse(userRepository.save(currentUser));
    }

    @Override
    @Transactional
    public void deleteUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        userRepository.deleteById(userId);
    }

    private User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private Role findRoleByName(String roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new InvalidRequestException("Role not found: " + roleName));
    }
}