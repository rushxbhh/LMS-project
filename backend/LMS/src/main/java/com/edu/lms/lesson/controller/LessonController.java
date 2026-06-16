package com.edu.lms.lesson.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.lesson.dto.CreateLessonRequest;
import com.edu.lms.lesson.dto.LessonDto;
import com.edu.lms.lesson.dto.UpdateLessonRequest;
import com.edu.lms.lesson.service.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;

    @PostMapping("/api/v1/modules/{moduleId}/lessons")
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
    public ApiResponse<String> deleteLesson(
            @PathVariable UUID id) {

        lessonService.deleteLesson(id);

        return ApiResponse.success(
                "Lesson deleted",
                "SUCCESS");
    }

    @GetMapping("/api/v1/lessons/{id}")
    public ApiResponse<LessonDto> getLesson(
            @PathVariable UUID id) {

        return ApiResponse.success(
                "Lesson fetched",
                lessonService.getLesson(id));
    }
}
