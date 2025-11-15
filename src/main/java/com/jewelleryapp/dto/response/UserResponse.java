package com.jewelleryapp.dto.response;

// Removed: import com.jewelleryapp.model.Role;
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
public class UserResponse {
    private UUID id; // Changed from Long to UUID
    private String firstName;
    private String lastName;
    private String email; // Renamed from username
    private Set<String> roles; // Changed from Role to Set<String>
}