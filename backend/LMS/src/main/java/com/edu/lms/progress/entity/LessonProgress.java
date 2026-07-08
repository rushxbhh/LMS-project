package com.edu.lms.progress.entity;

import com.edu.lms.lesson.entity.Lesson;
import com.edu.lms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "lesson_progress",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "student_id",
                                "lesson_id"
                        }
                )
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LessonProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    /**
     * Current playback position in seconds.
     * Used only for VIDEO lessons.
     */
    private Integer watchedSeconds;

    /**
     * 0-100
     */
    private Integer watchedPercentage;

    /**
     * Whether this lesson is completed.
     */
    private boolean completed;

    /**
     * Last time the student opened or watched this lesson.
     */
    private LocalDateTime lastAccessedAt;

    /**
     * Filled once completed=true.
     */
    private LocalDateTime completedAt;
}