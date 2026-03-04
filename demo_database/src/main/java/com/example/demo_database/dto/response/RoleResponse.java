package com.example.demo_database.dto.response;

import com.example.demo_database.entity.Permission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class RoleResponse {
    private String name;
    private String description;
    private Set<Permission> permissions;
}
