package com.edu.lms.enrollment.controller;

import com.edu.lms.common.exception.BusinessException;
import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.course.entity.Course;
import com.edu.lms.course.entity.CourseStatus;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.enrollment.dto.EnrolledCourseDto;
import com.edu.lms.enrollment.entity.Enrollment;
import com.edu.lms.enrollment.entity.EnrollmentStatus;
import com.edu.lms.enrollment.repository.EnrollmentRepository;
import com.edu.lms.enrollment.service.EnrollmentService;
import com.edu.lms.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollment", description = "Course enrollment management")
public class EnrollmentController {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentService enrollmentService;

    // ── Enroll ───────────────────────────────────────────────────────────────

    @PostMapping("/courses/{courseId}")
    @Operation(summary = "Enroll the current user in a course")
    public ResponseEntity<ApiResponse<Void>> enroll(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal User currentUser) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // FIX: Prevent enrollment in DRAFT or ARCHIVED courses
        if (course.getStatus() != CourseStatus.PUBLISHED) {
            throw new BusinessException("You can only enroll in published courses");
        }

        boolean alreadyEnrolled = enrollmentRepository
                .existsByStudentIdAndCourseIdAndStatus(
                        currentUser.getId(), courseId, EnrollmentStatus.ACTIVE);

        if (alreadyEnrolled) {
            throw new BusinessException("You are already enrolled in this course");
        }

        enrollmentRepository.save(Enrollment.builder()
                .student(currentUser)
                .course(course)
                .status(EnrollmentStatus.ACTIVE)
                .build());

        return ResponseEntity.ok(ApiResponse.success("Enrolled successfully"));
    }

    // ── Status check ─────────────────────────────────────────────────────────

    @GetMapping("/courses/{courseId}/status")
    @Operation(summary = "Check if the current user is enrolled in a course")
    public ResponseEntity<ApiResponse<EnrollmentStatusDto>> checkStatus(
            @PathVariable UUID courseId,
            @AuthenticationPrincipal User currentUser) {

        // FIX: Validate courseId exists — return 404 instead of false for bad IDs
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }

        if (currentUser == null) {
            return ResponseEntity.ok(
                    ApiResponse.success("Status checked", new EnrollmentStatusDto(false)));
        }

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                currentUser.getId(), courseId, EnrollmentStatus.ACTIVE);

        return ResponseEntity.ok(
                ApiResponse.success("Status checked", new EnrollmentStatusDto(enrolled)));
    }

    @GetMapping("/my")
    @Operation(summary = "My Learning — all enrolled courses, paginated, with progress")
    public ResponseEntity<ApiResponse<Page<EnrolledCourseDto>>> myLearning(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Fetched", enrollmentService.getMyLearning(currentUser, pageable)));
    }

    @GetMapping("/continue-learning")
    @Operation(summary = "Continue Learning — in-progress courses, most recently accessed first")
    public ResponseEntity<ApiResponse<List<EnrolledCourseDto>>> continueLearning(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "5") int limit) {

        return ResponseEntity.ok(ApiResponse.success("Fetched", enrollmentService.getContinueLearning(currentUser, limit)));
    }

    public record EnrollmentStatusDto(boolean enrolled) {}
}