package com.edu.lms.course.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.course.dto.CourseDto;
import com.edu.lms.course.dto.CreateCourseRequest;
import com.edu.lms.course.dto.UpdateCourseRequest;
import com.edu.lms.course.service.CourseService;
import com.edu.lms.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @Operation(summary = "get all published courses")
    @GetMapping
    public ApiResponse<List<CourseDto>> getCourses() {

        return ApiResponse.success(
                "Courses fetched",
                courseService.getAllPublishedCourses());
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
}