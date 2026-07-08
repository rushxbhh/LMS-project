package com.edu.lms.review.entity;

import com.edu.lms.course.entity.Course;
import com.edu.lms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "course_reviews",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}),
        indexes = {
                @Index(name = "idx_review_course", columnList = "course_id"),
                @Index(name = "idx_review_student", columnList = "student_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id")
    private Course course;

    /** 0.0–5.0, one decimal place e.g. 4.7 */
    @Column(nullable = false)
    private Double rating;

    @Column(length = 1000)
    private String comment;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}