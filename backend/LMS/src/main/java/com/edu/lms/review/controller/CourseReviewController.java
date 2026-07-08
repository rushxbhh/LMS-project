package com.edu.lms.review.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.review.dto.CourseReviewDto;
import com.edu.lms.review.dto.CreateReviewRequest;
import com.edu.lms.review.service.CourseReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses/{courseId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Course Reviews", description = "Ratings and reviews for a course")
public class CourseReviewController {

    private final CourseReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Submit or update your review for a course you're enrolled in")
    public ApiResponse<CourseReviewDto> submitReview(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateReviewRequest request) {
        return ApiResponse.success("Review submitted", reviewService.submitReview(courseId, request));
    }

    @GetMapping
    @Operation(summary = "List a course's reviews, paginated, newest first")
    public ApiResponse<Page<CourseReviewDto>> getReviews(
            @PathVariable UUID courseId,
            @PageableDefault(size = 10) Pageable pageable) {
        return ApiResponse.success("Reviews fetched", reviewService.getCourseReviews(courseId, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get your own review for this course, if any")
    public ApiResponse<Optional<CourseReviewDto>> getMyReview(@PathVariable UUID courseId) {
        return ApiResponse.success("Fetched", reviewService.getMyReview(courseId));
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Delete a review (owner or admin)")
    public ApiResponse<String> deleteReview(@PathVariable UUID courseId, @PathVariable UUID reviewId) {
        reviewService.deleteReview(courseId, reviewId);
        return ApiResponse.success("Review deleted", "SUCCESS");
    }
}