package com.edu.lms.lesson.dto;

import com.edu.lms.lesson.entity.LessonType;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class LessonDto {

    private UUID id;

    private String title;

    private LessonType type;

    private String videoUrl;

    private String content;

    private Integer durationMinutes;

    private Integer orderIndex;

    private Boolean freePreview;
}
