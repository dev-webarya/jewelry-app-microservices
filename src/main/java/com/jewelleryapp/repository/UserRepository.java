package com.jewelleryapp.repository;

import com.jewelleryapp.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID; // Import UUID

@Repository
public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> { // Changed Long to UUID
    Optional<User> findByEmail(String email); // Changed findByUsername to findByEmail
    boolean existsByEmail(String email); // Added for convenience
    Optional<User> findByPhoneNumber(String phoneNumber); // Added for validation
}