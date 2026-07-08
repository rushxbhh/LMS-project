package com.edu.lms.enrollment.service;

import com.edu.lms.enrollment.dto.EnrolledCourseDto;
import com.edu.lms.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EnrollmentService {
    Page<EnrolledCourseDto> getMyLearning(User student, Pageable pageable);
    List<EnrolledCourseDto> getContinueLearning(User student, int limit);
}