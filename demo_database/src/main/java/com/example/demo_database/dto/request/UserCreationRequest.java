package com.example.demo_database.dto.request;

import com.example.demo_database.exception.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
public class UserCreationRequest {
    @Size(min = 3, message = "USERNAME_INVALID")
    private String username;
    @Size(min = 6, message = "PASSWORD_INVALID")
    private String password;
    @NotBlank(message = "FirstName must be not blank")
    private String first_name;
    private String last_name;
    @NotNull(message = "Birthday must be not null")
    private LocalDate birth;

}
