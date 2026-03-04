package com.example.demo_database.mapper;

import com.example.demo_database.dto.request.UserCreationRequest;
import com.example.demo_database.dto.request.UserUpdateRequest;
import com.example.demo_database.dto.response.UserResponse;
import com.example.demo_database.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
//    @Mapping(target = "", source = "", ignore = true)
    UserResponse toUserResponse(User user);
    User toUser(UserCreationRequest request);
    List<UserResponse> toUserRepsponses(List<User> users);
    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdateRequest request);

}
