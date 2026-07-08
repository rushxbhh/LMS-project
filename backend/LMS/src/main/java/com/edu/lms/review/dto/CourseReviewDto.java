package com.edu.lms.review.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class CourseReviewDto {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private Double rating;
    private String comment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
