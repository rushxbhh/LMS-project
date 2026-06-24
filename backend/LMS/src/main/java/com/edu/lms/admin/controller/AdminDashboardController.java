package com.edu.lms.admin.controller;

import com.edu.lms.admin.dto.DashboardOverviewDto;
import com.edu.lms.admin.service.AdminDashboardService;
import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.course.dto.CourseEnrollmentSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Dashboard", description = "Platform-wide stats (Admin only)")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/overview")
    @Operation(summary = "User/course/application counts for the dashboard summary cards")
    public ResponseEntity<ApiResponse<DashboardOverviewDto>> getOverview() {
        return ResponseEntity.ok(ApiResponse.success("Overview fetched", dashboardService.getOverview()));
    }

    @GetMapping("/courses/enrollment-summary")
    @Operation(summary = "Student enrollment count per course")
    public ResponseEntity<ApiResponse<List<CourseEnrollmentSummaryDto>>> getEnrollmentSummary() {
        return ResponseEntity.ok(ApiResponse.success("Enrollment summary fetched",
                dashboardService.getEnrollmentSummary()));
    }
}