package com.edu.lms.user.dto;

import com.edu.lms.user.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID id;
    private String name;
    private String email;
    private User.Role role;
    private User.Provider provider;
    private String avatarUrl;
    private String bio;
    private LocalDateTime createdAt;
    private User.InstructorApplicationStatus instructorApplicationStatus;

    public static UserDto from(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .provider(user.getProvider())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .createdAt(user.getCreatedAt())
                .build();
    }
}