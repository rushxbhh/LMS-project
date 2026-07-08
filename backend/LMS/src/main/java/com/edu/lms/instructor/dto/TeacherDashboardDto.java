package com.edu.lms.teacher.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherDashboardDto {

    private long totalCourses;

    private long publishedCourses;

    private long draftCourses;

    private long pendingReviewCourses;

    private long freeCourses;

    private long paidCourses;

    private long totalEnrollments;

    private long totalStudents;
}
