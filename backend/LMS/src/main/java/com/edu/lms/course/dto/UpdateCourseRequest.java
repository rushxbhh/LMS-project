package com.edu.lms.course.dto;

import com.edu.lms.course.entity.CourseLevel;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

/**
 * All fields are optional — only non-null fields are applied in the service.
 * This gives PATCH-like semantics on a PUT endpoint so a partial update
 * cannot accidentally null out existing data.
 */
@Data
public class UpdateCourseRequest {
    private String title;
    private String description;
    private String thumbnailUrl;

    @PositiveOrZero(message = "Price must be 0 or greater")
    private BigDecimal price;

    private Boolean isFree;
    private CourseLevel level;
    private String category;
}