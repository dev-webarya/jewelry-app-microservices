package com.jewelleryapp.config;

import com.jewelleryapp.entity.Role;
import com.jewelleryapp.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("Checking for default roles...");

        // Use a list for cleaner checking
        List<String> requiredRoles = List.of("ROLE_CUSTOMER", "ROLE_ADMIN", "ROLE_STORE_MANAGER");

        for (String roleName : requiredRoles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                Role newRole = Role.builder().name(roleName).build();
                roleRepository.save(newRole);
                log.info("Created {}", roleName);
            }
        }

        log.info("Default roles check complete.");
    }
}