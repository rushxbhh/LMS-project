package com.edu.lms.admin.service;

import com.edu.lms.admin.dto.DashboardOverviewDto;
import com.edu.lms.course.dto.CourseEnrollmentSummaryDto;

import java.util.List;

public interface AdminDashboardService {
    DashboardOverviewDto getOverview();
    List<CourseEnrollmentSummaryDto> getEnrollmentSummary();
}