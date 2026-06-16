package com.edu.lms.module.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateModuleRequest {

    @NotBlank
    private String title;

    private Integer orderIndex;
}