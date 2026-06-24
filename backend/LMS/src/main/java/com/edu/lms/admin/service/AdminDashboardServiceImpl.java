package com.edu.lms.admin.service;

import com.edu.lms.admin.dto.DashboardOverviewDto;
import com.edu.lms.course.dto.CourseEnrollmentSummaryDto;
import com.edu.lms.course.entity.CourseStatus;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.instructor.entity.InstructorApplicationStatus;
import com.edu.lms.instructor.entity.InstructorApplicationType;
import com.edu.lms.instructor.repository.InstructorApplicationRepository;
import com.edu.lms.user.entity.User;
import com.edu.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final InstructorApplicationRepository applicationRepository;

    @Override
    @Transactional(readOnly = true)
    public DashboardOverviewDto getOverview() {
        return DashboardOverviewDto.builder()
                .totalStudents(userRepository.countByRole(User.Role.STUDENT))
                .totalTeachers(userRepository.countByRole(User.Role.TEACHER))
                .totalAdmins(userRepository.countByRole(User.Role.ADMIN))
                .totalCourses(courseRepository.count())
                .publishedCourses(courseRepository.countByStatus(CourseStatus.PUBLISHED))
                .draftCourses(courseRepository.countByStatus(CourseStatus.DRAFT))
                .pendingReviewCourses(courseRepository.countByStatus(CourseStatus.PENDING_REVIEW))
                .pendingExperiencedApplications(applicationRepository.countByTypeAndStatus(
                        InstructorApplicationType.EXPERIENCED, InstructorApplicationStatus.PENDING))
                .pendingNewbieApplications(applicationRepository.countByTypeAndStatus(
                        InstructorApplicationType.NEWBIE, InstructorApplicationStatus.PENDING))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseEnrollmentSummaryDto> getEnrollmentSummary() {
        return courseRepository.findEnrollmentSummary();
    }
}
