package com.edu.lms.course.dto;

import com.edu.lms.course.entity.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateCourseRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;
    private String thumbnailUrl;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be 0 or greater")
    private BigDecimal price;

    @NotNull(message = "isFree flag is required")
    private Boolean isFree;

    @NotNull(message = "Level is required")
    private CourseLevel level;

    @NotBlank(message = "Category is required")
    private String category;

    @NotNull(message = "Teacher ID is required")
    private UUID teacherId;
}