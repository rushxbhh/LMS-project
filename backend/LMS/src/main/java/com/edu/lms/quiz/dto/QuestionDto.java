package com.edu.lms.quiz.dto;

import com.edu.lms.quiz.entity.Question.QuestionType;
import lombok.Builder;
import lombok.Data;
import java.util.List;
import java.util.UUID;

@Data @Builder
public class QuestionDto {
    private UUID         id;
    private String       text;
    private QuestionType type;
    private Integer      points;
    private Integer      orderIndex;
    private String       explanation;
    private List<OptionDto> options;

    @Data @Builder
    public static class OptionDto {
        private String  text;
        private Boolean correct;
    }
}