package com.edu.lms.progress.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.progress.dto.CourseProgressDto;
import com.edu.lms.progress.dto.SaveProgressRequest;
import com.edu.lms.progress.service.ProgressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/progress")
@RequiredArgsConstructor
@Tag(name = "Progress", description = "Lesson and course progress tracking")
public class ProgressController {

    private final ProgressService progressService;

    @PostMapping("/lessons/{lessonId}/open")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Mark a lesson as opened / resumed")
    public ApiResponse<Void> openLesson(@PathVariable UUID lessonId) {
        progressService.openLesson(lessonId);
        return ApiResponse.success("Lesson opened");
    }

    @PatchMapping("/lessons/{lessonId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Save playback position / watch percentage")
    public ApiResponse<Void> saveProgress(
            @PathVariable UUID lessonId,
            @Valid @RequestBody SaveProgressRequest request) {
        progressService.saveProgress(lessonId, request);
        return ApiResponse.success("Progress saved");
    }

    @PostMapping("/lessons/{lessonId}/complete")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Mark a lesson as completed")
    public ApiResponse<Void> completeLesson(@PathVariable UUID lessonId) {
        progressService.completeLesson(lessonId);
        return ApiResponse.success("Lesson marked complete");
    }

    @GetMapping("/courses/{courseId}")
    @PreAuthorize("hasRole('STUDENT')")
    @Operation(summary = "Get current student's progress for a course")
    public ApiResponse<CourseProgressDto> getCourseProgress(@PathVariable UUID courseId) {
        return ApiResponse.success("Progress fetched", progressService.getCourseProgress(courseId));
    }
}