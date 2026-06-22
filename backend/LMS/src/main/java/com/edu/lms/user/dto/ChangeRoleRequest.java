package com.edu.lms.user.dto;

import com.edu.lms.user.entity.User;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoleRequest {

    @NotNull(message = "Role is required")
    private User.Role role;
}