package com.example.demo_database.service;

import com.example.demo_database.dto.request.PermissionRequest;
import com.example.demo_database.dto.request.RoleRequest;
import com.example.demo_database.dto.response.PermissionResponse;
import com.example.demo_database.dto.response.RoleResponse;
import com.example.demo_database.entity.Permission;
import com.example.demo_database.entity.Role;
import com.example.demo_database.mapper.PermissionMapper;
import com.example.demo_database.mapper.RoleMapper;
import com.example.demo_database.repository.PermissionRepository;
import com.example.demo_database.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
@Slf4j
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final PermissionRepository permissionRepository;
    public RoleResponse create(RoleRequest request){
        Role role = roleMapper.toRole(request);
        var permissions = permissionRepository.findAllById(request.getPermissions());
        role.setPermissions(new HashSet<>(permissions));
        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }
    public List<RoleResponse> getAll(){
        var roles = roleRepository.findAll();
        return roles.stream()
                .map(roleMapper::toRoleResponse)
                .toList();
    }
    public void delete(String name){
        roleRepository.deleteById(name);
    }
}
