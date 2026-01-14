package com.example.demo_database.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data @AllArgsConstructor @Builder
@NoArgsConstructor
// final không dùng được với @NoArgsConstructor --> cần xem xét
public class AuthenticationRequest {
    private String username;
    private String password;
}
