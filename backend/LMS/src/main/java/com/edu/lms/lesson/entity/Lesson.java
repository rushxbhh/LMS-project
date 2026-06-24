package com.edu.lms.lesson.entity;

import com.edu.lms.module.entity.CourseModule;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "lessons", indexes = {
        @Index(name = "idx_lesson_module_id",    columnList = "module_id"),
        @Index(name = "idx_lesson_module_order", columnList = "module_id, order_index")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    private LessonType type;

    private String videoUrl;

    @Column(length = 10000)
    private String content;

    private Integer durationMinutes;

    private Integer orderIndex;

    @Builder.Default
    private Boolean freePreview = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id")
    private CourseModule module;
}