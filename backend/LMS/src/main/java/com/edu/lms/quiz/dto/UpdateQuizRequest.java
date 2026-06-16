package com.edu.lms.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateQuizRequest {
    private String  title;
    private String  description;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    @Min(0) @Max(100)
    private Integer passingScore;
    private Boolean shuffleQuestions;
}