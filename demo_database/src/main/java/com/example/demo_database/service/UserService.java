package com.example.demo_database.service;

import com.example.demo_database.dto.request.UserCreationRequest;
import com.example.demo_database.dto.request.UserUpdateRequest;
import com.example.demo_database.dto.response.UserResponse;
import com.example.demo_database.entity.User;
import com.example.demo_database.exception.ErrorCode;
import com.example.demo_database.exception.MyAppException;
import com.example.demo_database.mapper.UserMapper;
import com.example.demo_database.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    public User createUser(UserCreationRequest request){
        if (userRepository.existsByUsername(request.getUsername())){
            throw new MyAppException(ErrorCode.USER_EXISTED);
        }
        User user = userMapper.createUser(request);
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    public List<User> getUsers(){
        return userRepository.findAll();
    }
    public UserResponse getUserById(String userId){
        return userMapper.toUserResponse(userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found")));
    }
    public UserResponse updateUser(String userId, UserUpdateRequest request){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userMapper.updateUser(user, request);

        return userMapper.toUserResponse(userRepository.save(user));
    }
    public void deleteUser(String userId){
        userRepository.deleteById(userId);
    }
}
