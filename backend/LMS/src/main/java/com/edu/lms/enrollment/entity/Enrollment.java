package com.edu.lms.enrollment.entity;

import com.edu.lms.course.entity.Course;
import com.edu.lms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "enrollments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "course_id"}),
        indexes = {
                @Index(name = "idx_enroll_student",        columnList = "student_id"),
                @Index(name = "idx_enroll_course",         columnList = "course_id"),
                @Index(name = "idx_enroll_student_course", columnList = "student_id, course_id"),
                @Index(name = "idx_enroll_student_status", columnList = "student_id, status")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrolled_at", nullable = false, updatable = false)
    private LocalDateTime enrolledAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.ACTIVE;


    @PrePersist
    protected void onCreate() {
        enrolledAt = LocalDateTime.now();
    }
}