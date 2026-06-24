package com.edu.lms.instructor.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RejectApplicationRequest {

    @NotBlank(message = "A rejection reason is required so the applicant knows what to improve")
    private String reason;
}
