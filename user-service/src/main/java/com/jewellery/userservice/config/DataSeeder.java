package com.jewellery.userservice.config;

import com.jewellery.userservice.entity.Role;
import com.jewellery.userservice.entity.User;
import com.jewellery.userservice.repository.RoleRepository;
import com.jewellery.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Development-only data seeder for user-service.
 * Seeds roles and sample users (admin, store managers).
 * Will NOT run in production (requires "dev" profile).
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Seed Roles (always needed)
        Map<String, Role> roles = seedRoles();

        // 2. Check if users already exist
        if (userRepository.count() > 0) {
            log.info("Users already exist. Skipping user seeding.");
            return;
        }

        // 3. Seed sample users
        seedUsers(roles);
        log.info("User service data seeding complete.");
    }

    private Map<String, Role> seedRoles() {
        log.info("Checking for default roles...");
        List<String> requiredRoles = List.of("ROLE_CUSTOMER", "ROLE_ADMIN", "ROLE_STORE_MANAGER");
        return requiredRoles.stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseGet(() -> {
                            log.info("Creating role: {}", roleName);
                            return roleRepository.save(Role.builder().name(roleName).build());
                        }))
                .collect(Collectors.toMap(Role::getName, role -> role));
    }

    private void seedUsers(Map<String, Role> roles) {
        log.info("Seeding sample users (Admin and Store Managers)...");

        // Admin User
        userRepository.save(User.builder()
                .firstName("Admin")
                .lastName("User")
                .email("admin@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("9999999999")
                .roles(Set.of(roles.get("ROLE_ADMIN"), roles.get("ROLE_CUSTOMER")))
                .isEnabled(true)
                .build());

        // Store Manager for Main Street
        userRepository.save(User.builder()
                .firstName("Main")
                .lastName("Manager")
                .email("manager.main@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("8888888888")
                .roles(Set.of(roles.get("ROLE_STORE_MANAGER"), roles.get("ROLE_CUSTOMER")))
                .isEnabled(true)
                .build());

        // Store Manager for Mall Branch
        userRepository.save(User.builder()
                .firstName("Mall")
                .lastName("Manager")
                .email("manager.mall@app.com")
                .password(passwordEncoder.encode("Password@123"))
                .phoneNumber("7777777777")
                .roles(Set.of(roles.get("ROLE_STORE_MANAGER"), roles.get("ROLE_CUSTOMER")))
                .isEnabled(true)
                .build());

        log.info("Seeded 3 users: admin@app.com, manager.main@app.com, manager.mall@app.com");
    }
}
