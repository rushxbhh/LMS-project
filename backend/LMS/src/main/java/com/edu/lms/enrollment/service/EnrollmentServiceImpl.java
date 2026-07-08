package com.edu.lms.enrollment.service;

import com.edu.lms.course.entity.Course;
import com.edu.lms.enrollment.dto.EnrolledCourseDto;
import com.edu.lms.enrollment.entity.Enrollment;
import com.edu.lms.enrollment.entity.EnrollmentStatus;
import com.edu.lms.enrollment.repository.EnrollmentRepository;
import com.edu.lms.progress.entity.CourseProgress;
import com.edu.lms.progress.repository.CourseProgressRepository;
import com.edu.lms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EnrollmentServiceImpl implements EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseProgressRepository courseProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EnrolledCourseDto> getMyLearning(User student, Pageable pageable) {
        Page<Enrollment> enrollments = enrollmentRepository
                .findByStudentIdAndStatusOrderByEnrolledAtDesc(student.getId(), EnrollmentStatus.ACTIVE, pageable);

        return enrollments.map(e -> toDto(student, e.getCourse(), e.getEnrolledAt()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrolledCourseDto> getContinueLearning(User student, int limit) {
        List<CourseProgress> inProgress = courseProgressRepository
                .findContinueLearning(student, PageRequest.of(0, limit));

        return inProgress.stream()
                .map(cp -> toDto(student, cp.getCourse(), null))
                .toList();
    }

    private EnrolledCourseDto toDto(User student, Course course, LocalDateTime enrolledAt) {
        Optional<CourseProgress> progress = courseProgressRepository.findByStudentAndCourse(student, course);

        return EnrolledCourseDto.builder()
                .courseId(course.getId())
                .title(course.getTitle())
                .thumbnailUrl(course.getThumbnailUrl())
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .totalLessons(course.getTotalLessons())
                .completedLessons(progress.map(CourseProgress::getCompletedLessons).orElse(0))
                .progressPercentage(progress.map(CourseProgress::getProgressPercentage).orElse(0))
                .lastLessonId(progress.map(CourseProgress::getLastLesson).map(l -> l.getId()).orElse(null))
                .completed(progress.map(CourseProgress::isCompleted).orElse(false))
                .enrolledAt(enrolledAt)
                .lastAccessedAt(progress.map(CourseProgress::getLastAccessedAt).orElse(null))
                .build();
    }
}