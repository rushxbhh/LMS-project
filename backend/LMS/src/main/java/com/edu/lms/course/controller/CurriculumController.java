package com.edu.lms.course.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.course.service.CurriculumService;
import com.edu.lms.course.dto.CurriculumDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
@Tag(name = "Curriculum", description = "Course curriculum with enrollment-gated access")
public class CurriculumController {

    private final CurriculumService curriculumService;

    @GetMapping("/{courseId}/curriculum")
    @Operation(summary = "Get course curriculum — free previews for guests, full list for enrolled users")
    public ResponseEntity<ApiResponse<CurriculumDto>> getCurriculum(
            @PathVariable UUID courseId) {

        CurriculumDto curriculum = curriculumService.getCurriculum(courseId);
        return ResponseEntity.ok(ApiResponse.success("fetched sucessfully", curriculum));
    }
}
