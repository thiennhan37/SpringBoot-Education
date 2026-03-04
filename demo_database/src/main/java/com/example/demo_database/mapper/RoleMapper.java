package com.example.demo_database.mapper;

import com.example.demo_database.dto.request.RoleRequest;
import com.example.demo_database.dto.response.RoleResponse;
import com.example.demo_database.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);
    RoleResponse toRoleResponse(Role role);


}
