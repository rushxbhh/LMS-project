package com.edu.lms.quiz.dto;

import com.edu.lms.quiz.entity.Question.QuestionType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class CreateQuestionRequest {
    @NotBlank(message = "Question text is required")
    private String       text;
    @NotNull(message = "Question type is required")
    private QuestionType type;
    @Min(1)
    private Integer      points;
    private Integer      orderIndex;
    private String       explanation;
    private List<OptionRequest> options;

    @Data
    public static class OptionRequest {
        @NotBlank
        private String  text;
        private Boolean correct;
    }
}