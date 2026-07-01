package com.edu.lms.instructor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class SubmitNewbieApplicationRequest {

    @NotNull(message = "courseId is required")
    private UUID courseId;

    @NotBlank
    private String resumeUrl;
}
