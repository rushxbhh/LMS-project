package com.edu.lms.enrollment.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class EnrolledCourseDto {
    private UUID courseId;
    private String title;
    private String thumbnailUrl;
    private String teacherName;
    private Integer totalLessons;
    private Integer completedLessons;
    private Integer progressPercentage;
    private UUID lastLessonId;
    private boolean completed;
    private LocalDateTime enrolledAt;
    private LocalDateTime lastAccessedAt;
}