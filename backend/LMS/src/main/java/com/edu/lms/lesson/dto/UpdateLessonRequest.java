package com.edu.lms.lesson.dto;

import com.edu.lms.lesson.entity.LessonType;
import lombok.Data;

@Data
public class UpdateLessonRequest {

    private String title;

    private LessonType type;

    private String videoUrl;

    private String content;

    private Integer durationMinutes;

    private Integer orderIndex;

    private Boolean freePreview;
}
