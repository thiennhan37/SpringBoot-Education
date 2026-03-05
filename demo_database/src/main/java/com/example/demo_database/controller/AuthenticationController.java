package com.example.demo_database.controller;

import com.example.demo_database.dto.request.IntrospectRequest;
import com.example.demo_database.dto.request.LogoutRequest;
import com.example.demo_database.dto.request.RefreshTokenRequest;
import com.example.demo_database.dto.response.ApiResponse;
import com.example.demo_database.dto.request.AuthenticationRequest;
import com.example.demo_database.dto.response.AuthenticationResponse;
import com.example.demo_database.dto.response.IntrospectResponse;
import com.example.demo_database.dto.response.RefreshResponse;
import com.example.demo_database.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;


@Slf4j
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
    public ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.introspect(request);
        return ApiResponse.<IntrospectResponse>builder()
                .result(result)
                .build();
    }
//    @PostMapping("/logout")
//    public ApiResponse<Void> logout(@RequestBody LogoutRequest request) throws ParseException, JOSEException {
//        authenticationService.logout(request);
//        return ApiResponse.<Void>builder()
//                .build();
//    }
    @PostMapping
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String refreshToken)
            throws ParseException, JOSEException
    {
        log.info(refreshToken);
//        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
    }
    @PostMapping("/refresh")
    public ApiResponse<RefreshResponse> refreshToken(@RequestBody RefreshTokenRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);
        return ApiResponse.<RefreshResponse>builder()
                .result(result)
                .build();
    }
}
