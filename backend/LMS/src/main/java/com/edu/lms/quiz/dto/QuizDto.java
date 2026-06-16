package com.edu.lms.quiz.dto;

import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data @Builder
public class QuizDto {
    private UUID    id;
    private String  title;
    private String  description;
    private Integer timeLimitMinutes;
    private Integer maxAttempts;
    private Integer passingScore;
    private Boolean shuffleQuestions;
    private Boolean published;
    private UUID    lessonId;
    private Integer questionCount;
    private Integer totalPoints;
}