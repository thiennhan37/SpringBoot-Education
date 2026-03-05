package com.example.demo_database.dto.JwtObject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtInfo {
    @Id
    private String jwtId;
    private Date issueTime;
    private Date expirationTime;
}
