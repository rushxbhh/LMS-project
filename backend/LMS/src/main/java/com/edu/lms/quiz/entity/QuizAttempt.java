package com.edu.lms.quiz.entity;

import com.edu.lms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "quiz_attempts", indexes = {
        @Index(name = "idx_attempt_student_quiz", columnList = "student_id, quiz_id"),
        @Index(name = "idx_attempt_student",      columnList = "student_id")
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false) @Builder.Default private Integer score      = 0;
    @Column(nullable = false) @Builder.Default private Integer maxScore   = 0;
    @Column(nullable = false) @Builder.Default private Integer percentage = 0;
    @Column(nullable = false) @Builder.Default private Boolean  passed    = false;

    private Integer timeTakenSeconds;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @CreationTimestamp private LocalDateTime startedAt;
    private LocalDateTime completedAt;

    public enum AttemptStatus { IN_PROGRESS, COMPLETED, TIMED_OUT }
}