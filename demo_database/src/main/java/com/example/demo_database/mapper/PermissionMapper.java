package com.example.demo_database.mapper;

import com.example.demo_database.dto.request.PermissionRequest;
import com.example.demo_database.dto.response.PermissionResponse;
import com.example.demo_database.entity.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
//    @Mapping(target = "", source = "", ignore = true)
    Permission toPermission(PermissionRequest request);
    PermissionResponse toPermissionResponse(Permission permission);



}
