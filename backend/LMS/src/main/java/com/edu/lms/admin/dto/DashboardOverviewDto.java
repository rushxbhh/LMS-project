package com.edu.lms.admin.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardOverviewDto {
    private long totalStudents;
    private long totalTeachers;
    private long totalAdmins;
    private long totalCourses;
    private long publishedCourses;
    private long draftCourses;
    private long pendingReviewCourses;
    private long pendingExperiencedApplications;
    private long pendingNewbieApplications;
}
