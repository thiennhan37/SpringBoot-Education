package com.example.demo_database.dto.response;

import com.example.demo_database.entity.Role;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String username, first_name, last_name;
    private LocalDate birth;
    private Set<Role> roles;
}
