package com.edu.lms.course.entity;

import com.edu.lms.enrollment.entity.Enrollment;
import com.edu.lms.module.entity.CourseModule;
import com.edu.lms.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "courses", indexes = {
        @Index(name = "idx_courses_status",     columnList = "status"),
        @Index(name = "idx_courses_teacher_id", columnList = "teacher_id"),
        @Index(name = "idx_courses_level",   columnList = "level")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(length = 5000)
    private String description;

    private String thumbnailUrl;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Boolean isFree;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseLevel level;

    @Column(nullable = false)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;

    // ── NEW: modules relationship ──────────────────────────────────────────
    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<CourseModule> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Enrollment> enrollments = new ArrayList<>();


    @Builder.Default
    private Integer totalLessons = 0;

    @Builder.Default
    private Integer totalDurationMinutes = 0;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}