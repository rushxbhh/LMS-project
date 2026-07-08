package com.edu.lms.course.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.course.dto.CourseDto;
import com.edu.lms.course.dto.CreateCourseRequest;
import com.edu.lms.course.dto.UpdateCourseRequest;
import com.edu.lms.course.entity.CourseLevel;
import com.edu.lms.course.entity.CourseStatus;
import com.edu.lms.course.service.CourseService;
import com.edu.lms.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;


    @Operation(summary = "Create the course")
    @PostMapping
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<CourseDto> createCourse(
            @RequestBody CreateCourseRequest request, @AuthenticationPrincipal User currentUser) {

        return ApiResponse.success(
                "Course created",
                courseService.createCourse(request, currentUser));
    }

    @Operation(summary = "Search/browse published courses — filter by category, level, price range; sort by price, enrolledCount, or averageRating")
    @GetMapping
    public ApiResponse<Page<CourseDto>> getCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) CourseLevel level,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ApiResponse.success("Courses fetched",
                courseService.searchCourses(search, category, level, minPrice, maxPrice, pageable));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Get all courses owned by the logged in teacher")
    public ApiResponse<List<CourseDto>> getMyCourses(

            @RequestParam(required = false)
            CourseStatus status

    ) {

        return ApiResponse.success(
                "Teacher courses fetched",
                courseService.getMyCourses(status)
        );
    }


    @Operation(summary = "get the course by id")
    @GetMapping("/{id}")
    public ApiResponse<CourseDto> getCourse(
            @PathVariable UUID id) {

        return ApiResponse.success(
                "Course fetched",
                courseService.getCourseById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "update the course by id")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<CourseDto> updateCourse(
            @PathVariable UUID id,
            @RequestBody UpdateCourseRequest request) {

        return ApiResponse.success(
                "Course updated",
                courseService.updateCourse(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "delete the course by id")
    @PreAuthorize("hasRole('TEACHER')")
    public ApiResponse<String> deleteCourse(
            @PathVariable UUID id) {

        courseService.deleteCourse(id);

        return ApiResponse.success(
                "Course deleted",
                "SUCCESS");
    }

    @PostMapping("/{id}/publish")
    @Operation(summary = "publish course")
    public ApiResponse<CourseDto> publishCourse(
            @PathVariable UUID id) {

        return ApiResponse.success(
                "Course published",
                courseService.publishCourse(id));
    }

    @PostMapping("/{id}/submit-for-review")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Submit a draft course for admin review")
    public ApiResponse<CourseDto> submitForReview(@PathVariable UUID id) {
        return ApiResponse.success("Course submitted for review", courseService.submitForReview(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve a pending-review course (Admin only)")
    public ApiResponse<CourseDto> approveCourse(@PathVariable UUID id) {
        return ApiResponse.success("Course approved and published", courseService.approveCourse(id));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject a pending-review course, sends it back to draft (Admin only)")
    public ApiResponse<CourseDto> rejectCourse(@PathVariable UUID id, @RequestParam(required = false) String reason) {
        return ApiResponse.success("Course rejected", courseService.rejectCourse(id, reason));
    }

    @GetMapping("/admin/pending-review")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all courses pending review (Admin only)")
    public ApiResponse<List<CourseDto>> getPendingReviewCourses() {
        return ApiResponse.success("Pending review courses fetched",
                courseService.getCoursesByStatus(CourseStatus.PENDING_REVIEW));
    }
}