package com.edu.lms.instructor.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.instructor.dto.*;
import com.edu.lms.instructor.entity.InstructorApplicationStatus;
import com.edu.lms.instructor.entity.InstructorApplicationType;
import com.edu.lms.instructor.service.InstructorApplicationService;
import com.edu.lms.user.entity.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructor-applications")
@RequiredArgsConstructor
@Tag(name = "Instructor Applications", description = "Student-to-instructor onboarding workflow")
public class InstructorApplicationController {

    private final InstructorApplicationService applicationService;

    // ── Student-facing ───────────────────────────────────────────────────────

    @PostMapping("/experienced")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Apply as an experienced instructor")
    public ResponseEntity<ApiResponse<InstructorApplicationDto>> applyExperienced(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SubmitExperiencedApplicationRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Application submitted",
                applicationService.submitExperienced(currentUser.getId(), request)));
    }

    @PostMapping("/newbie")
    @PreAuthorize("hasRole('TEACHER')")
    @Operation(summary = "Apply as a newbie instructor by submitting a course for review")
    public ResponseEntity<ApiResponse<InstructorApplicationDto>> applyNewbie(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SubmitNewbieApplicationRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Course submitted for review",
                applicationService.submitNewbie(currentUser.getId(), request)));
    }

    @GetMapping("/me")
    @Operation(summary = "Get the current user's instructor application history")
    public ResponseEntity<ApiResponse<List<InstructorApplicationDto>>> getMyApplications(
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getMyApplications(currentUser.getId())));
    }

    // ── Admin-facing ─────────────────────────────────────────────────────────

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List instructor applications, optionally filtered by type/status (Admin only)")
    public ResponseEntity<ApiResponse<Page<InstructorApplicationDto>>> getApplications(
            @RequestParam(required = false) InstructorApplicationType type,
            @RequestParam(required = false) InstructorApplicationStatus status,
            Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.success("Applications fetched",
                applicationService.getApplicationsForAdmin(type, status, pageable)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Approve an instructor application (Admin only)")
    public ResponseEntity<ApiResponse<InstructorApplicationDto>> approve(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentAdmin,
            @RequestBody(required = false) ApproveApplicationRequest request) {

        String note = request != null ? request.getNote() : null;
        return ResponseEntity.ok(ApiResponse.success("Application approved",
                applicationService.approve(id, currentAdmin.getId(), note)));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reject an instructor application (Admin only)")
    public ResponseEntity<ApiResponse<InstructorApplicationDto>> reject(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentAdmin,
            @Valid @RequestBody RejectApplicationRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Application rejected",
                applicationService.reject(id, currentAdmin.getId(), request.getReason())));
    }
}