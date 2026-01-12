package com.example.demo_database.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserUpdateRequest {
    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;
    private String first_name, last_name;
    private LocalDate birth;
}
