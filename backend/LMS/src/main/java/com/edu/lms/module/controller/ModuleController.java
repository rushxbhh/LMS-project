package com.edu.lms.module.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.module.dto.CreateModuleRequest;
import com.edu.lms.module.dto.ModuleDto;
import com.edu.lms.module.dto.UpdateModuleRequest;
import com.edu.lms.module.service.ModuleService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping("/{courseId}/modules")
    @Operation(summary = "Create a module within a course")
    public ApiResponse<ModuleDto> createModule(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateModuleRequest request) {

        return ApiResponse.success("Module created",
                moduleService.createModule(courseId, request));
    }

    @PutMapping("/{courseId}/modules/{moduleId}")
    @Operation(summary = "Update module — validates courseId matches module's course")
    public ApiResponse<ModuleDto> updateModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId,
            @Valid @RequestBody UpdateModuleRequest request) {

        // FIX: courseId is now passed to and validated inside the service
        return ApiResponse.success("Module updated",
                moduleService.updateModule(courseId, moduleId, request));
    }

    @DeleteMapping("/{courseId}/modules/{moduleId}")
    @Operation(summary = "Delete a module and cascade its lessons")
    public ApiResponse<String> deleteModule(
            @PathVariable UUID courseId,
            @PathVariable UUID moduleId) {

        // FIX: courseId is now passed to and validated inside the service
        moduleService.deleteModule(courseId, moduleId);
        return ApiResponse.success("Module deleted", "SUCCESS");
    }
}