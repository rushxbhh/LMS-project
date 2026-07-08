package com.edu.lms.progress.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CourseProgressDto {

    private UUID courseId;

    private Integer progressPercentage;

    private Integer completedLessons;

    private Integer totalLessons;

    private UUID lastLessonId;

    private boolean completed;
}
