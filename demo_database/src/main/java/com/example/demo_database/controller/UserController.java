package com.example.demo_database.controller;

import com.example.demo_database.dto.response.ApiResponse;
import com.example.demo_database.dto.request.UserCreationRequest;
import com.example.demo_database.dto.request.UserUpdateRequest;
import com.example.demo_database.dto.response.UserResponse;
import com.example.demo_database.entity.User;
import com.example.demo_database.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/users")
@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PostMapping
    ApiResponse<User> createUser(@RequestBody @Valid UserCreationRequest request){
        ApiResponse<User> apiResponse = new ApiResponse<User>();
        apiResponse.setResult(userService.createUser(request));
        return apiResponse;
    }
    @GetMapping()
    List<User> getUsers(){
        return userService.getUsers();
    }
    @GetMapping("/{userId}")
    UserResponse getUserById(@PathVariable("userId") String userId){
        return userService.getUserById(userId);
    }
    @PutMapping("/{userId}")
    UserResponse updateUser(@PathVariable("userId") String userId, @RequestBody @Valid UserUpdateRequest request){
        return userService.updateUser(userId, request);
    }
    @DeleteMapping("{userId}")
    String deleteUser(@PathVariable("userId") String userId){
        userService.deleteUser(userId);
        return "User has been deleted";
    }
//    @RequestMapping(value = "/users", method = RequestMethod.DELETE)
}
