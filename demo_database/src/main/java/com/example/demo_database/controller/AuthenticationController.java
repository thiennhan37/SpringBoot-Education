package com.example.demo_database.controller;

import com.example.demo_database.dto.request.IntrospectRequest;
import com.example.demo_database.dto.response.ApiResponse;
import com.example.demo_database.dto.request.AuthenticationRequest;
import com.example.demo_database.dto.response.AuthenticationResponse;
import com.example.demo_database.dto.response.IntrospectResponse;
import com.example.demo_database.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @PostMapping("/log-in")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
    }

    @PostMapping("/introspect") // xác thực token
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request){
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
}
