package com.edu.lms.course.dto;

import com.edu.lms.course.entity.CourseStatus;
import lombok.Value;

import java.util.UUID;

@Value
public class CourseEnrollmentSummaryDto {
    UUID courseId;
    String title;
    CourseStatus status;
    Long enrolledStudents;
}
