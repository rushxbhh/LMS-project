package com.edu.lms.review.service;

import com.edu.lms.review.dto.CourseReviewDto;
import com.edu.lms.review.dto.CreateReviewRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface CourseReviewService {
    CourseReviewDto submitReview(UUID courseId, CreateReviewRequest request);
    void deleteReview(UUID courseId, UUID reviewId);
    Page<CourseReviewDto> getCourseReviews(UUID courseId, Pageable pageable);
    Optional<CourseReviewDto> getMyReview(UUID courseId);
}