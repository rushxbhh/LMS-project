package com.edu.lms.quiz.entity;

import com.edu.lms.course.entity.Lesson;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "quizzes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer timeLimitMinutes;   // null = no limit
    private Integer maxAttempts;        // null = unlimited

    @Column(nullable = false)
    @Builder.Default
    private Integer passingScore = 70;  // percentage 0-100

    @Column(nullable = false)
    @Builder.Default
    private Boolean shuffleQuestions = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean published = false;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", unique = true)
    private Lesson lesson;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @CreationTimestamp private LocalDateTime createdAt;
    @UpdateTimestamp  private LocalDateTime updatedAt;
}