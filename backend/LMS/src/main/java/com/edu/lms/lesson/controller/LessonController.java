package com.edu.lms.lesson.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.lesson.dto.CreateLessonRequest;
import com.edu.lms.lesson.dto.LessonDto;
import com.edu.lms.lesson.dto.UpdateLessonRequest;
import com.edu.lms.lesson.service.LessonService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/api/v1/modules/{moduleId}/lessons")
    @Operation(summary = "create the course with the course module with id")
    public ApiResponse<LessonDto> createLesson(
            @PathVariable UUID moduleId,
            @RequestBody CreateLessonRequest request) {

        return ApiResponse.success(
                "Lesson created",
                lessonService.createLesson(
                        moduleId,
                        request));
    }

    @PutMapping("/api/v1/lessons/{id}")
    @Operation(summary = "update the lesson with id")
    public ApiResponse<LessonDto> updateLesson(
            @PathVariable UUID id,
            @RequestBody UpdateLessonRequest request) {

        return ApiResponse.success(
                "Lesson updated",
                lessonService.updateLesson(
                        id,
                        request));
    }

    @DeleteMapping("/api/v1/lessons/{id}")
    @Operation(summary = "delete the course with id")
    public ApiResponse<String> deleteLesson(
            @PathVariable UUID id) {

        lessonService.deleteLesson(id);

        return ApiResponse.success(
                "Lesson deleted",
                "SUCCESS");
    }

    @GetMapping("/api/v1/lessons/{id}")
    @Operation(summary = "get the lesson with id")
    public ApiResponse<LessonDto> getLesson(
            @PathVariable UUID id) {

        return ApiResponse.success(
                "Lesson fetched",
                lessonService.getLesson(id));
    }

    @PostMapping(value = "/api/v1/lessons/{id}/video", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @Operation(summary = "Upload or replace the video for a lesson")
    public ApiResponse<LessonDto> uploadLessonVideo(
            @PathVariable UUID id,
            @RequestParam("file") MultipartFile file) {
        return ApiResponse.success("Video uploaded", lessonService.uploadLessonVideo(id, file));
    }

    @DeleteMapping("/api/v1/lessons/{id}/video")
    @PreAuthorize("hasAnyRole('TEACHER','ADMIN')")
    @Operation(summary = "Delete the video attached to a lesson")
    public ApiResponse<String> deleteLessonVideo(@PathVariable UUID id) {
        lessonService.deleteLessonVideo(id);
        return ApiResponse.success("Video deleted", "SUCCESS");
    }

    @GetMapping("/api/v1/lessons/{id}/video-url")
    @Operation(summary = "Get a short-lived streaming URL for the lesson video")
    public ApiResponse<String> getLessonVideoUrl(@PathVariable UUID id) {
        return ApiResponse.success("Video URL generated", lessonService.getLessonVideoUrl(id));
    }
}
