package com.edu.lms.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.UUID;

@Data
public class CreateQuizRequest {
    @NotBlank(message = "Title is required")
    private String  title;
    private String  description;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    @Min(0) @Max(100)
    private Integer passingScore;
    private Boolean shuffleQuestions;
    private UUID    lessonId;           // optional — attach to a QUIZ-type lesson
}