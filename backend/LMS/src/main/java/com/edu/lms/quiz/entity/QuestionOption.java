package com.edu.lms.quiz.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuestionOption {

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false)
    @Builder.Default
    private Boolean correct = false;
}