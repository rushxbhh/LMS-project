package com.edu.lms.course.dto;

import com.edu.lms.course.entity.CourseLevel;
import com.edu.lms.course.entity.CourseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDto {

    private UUID id;

    private String title;

    private String description;

    private String thumbnailUrl;

    private BigDecimal price;

    private Boolean isFree;

    private CourseLevel level;

    private String category;

    private CourseStatus status;

    private Integer totalLessons;

    private Integer totalDurationMinutes;
}
