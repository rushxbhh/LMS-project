package com.edu.lms.quiz.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class AttemptResultDto {
    private UUID            attemptId;
    private UUID            quizId;
    private String          quizTitle;
    private Integer         score;
    private Integer         maxScore;
    private Integer         percentage;
    private Boolean         passed;
    private Integer         timeTakenSeconds;
    private LocalDateTime   completedAt;
    private List<QuestionResultDto> questionResults;

    @Data @Builder
    public static class QuestionResultDto {
        private UUID         questionId;
        private String       questionText;
        private List<Integer> chosenIndices;
        private List<Integer> correctIndices;
        private Boolean      correct;
        private Integer      pointsEarned;
        private Integer      pointsAvailable;
        private String       explanation;
    }
}