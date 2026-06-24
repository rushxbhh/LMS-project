package com.edu.lms.instructor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SubmitExperiencedApplicationRequest {

    @NotBlank(message = "Resume/CV link is required")
    private String resumeUrl;

    @NotBlank(message = "LinkedIn profile is required")
    private String linkedinUrl;

    @NotBlank(message = "Tell us about your teaching experience")
    @Size(max = 3000, message = "Keep it under 3000 characters")
    private String teachingExperience;

}