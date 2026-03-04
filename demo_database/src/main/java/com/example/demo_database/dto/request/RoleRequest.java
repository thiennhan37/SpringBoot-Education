package com.example.demo_database.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RoleRequest {
    private String name;
    private String description;
    private Set<String> permissions;
}
