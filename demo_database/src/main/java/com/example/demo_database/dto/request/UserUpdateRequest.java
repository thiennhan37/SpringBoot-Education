package com.example.demo_database.dto.request;

import com.example.demo_database.validatior.BirthConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class UserUpdateRequest {
    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;
    private String first_name, last_name;

    @BirthConstraint(min = 18, message = "BIRTHDAY_INVALID")
    private LocalDate birth;
    private Set<String> roles; // .. List
}
