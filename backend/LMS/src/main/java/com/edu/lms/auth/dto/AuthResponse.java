package com.edu.lms.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String refreshToken;
    private String userId;
    private String email;
    private String name;
    private String role;
    private String avatarUrl;
    private String instructorApplicationStatus;
}