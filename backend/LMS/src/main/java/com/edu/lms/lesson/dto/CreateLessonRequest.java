package com.edu.lms.lesson.dto;

import com.edu.lms.lesson.entity.LessonType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLessonRequest {

    @NotBlank(message = "Lesson title is required")
    private String title;

    @NotNull(message = "Lesson type is required")
    private LessonType type;

    private String videoUrl;
    private String content;

    @Min(value = 0, message = "Duration must be 0 or greater")
    private Integer durationMinutes;

    private Integer orderIndex;
    private Boolean freePreview;

    /** Cross-field rule: videoUrl must be provided when type is VIDEO */
    @AssertTrue(message = "videoUrl is required for VIDEO type lessons")
    private boolean isVideoUrlValid() {
        if (type == LessonType.VIDEO) {
            return videoUrl != null && !videoUrl.isBlank();
        }
        return true;
    }
}