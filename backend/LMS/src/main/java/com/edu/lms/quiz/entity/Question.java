package com.edu.lms.quiz.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quiz_questions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 2000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)
    @Builder.Default
    private Integer points = 1;

    private Integer orderIndex;

    @Column(length = 2000)
    private String explanation;     // shown after student answers

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ElementCollection
    @CollectionTable(name = "question_options", joinColumns = @JoinColumn(name = "question_id"))
    @Builder.Default
    private List<QuestionOption> options = new ArrayList<>();

    public enum QuestionType {
        SINGLE_CHOICE,   // exactly one correct answer
        MULTIPLE_CHOICE, // one or more correct answers
        TRUE_FALSE       // auto-generates True/False options
    }
}