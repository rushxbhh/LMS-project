package com.edu.lms.course.service;

import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.course.dto.CurriculumDto;
import com.edu.lms.lesson.dto.LessonCurriculumDto;
import com.edu.lms.module.dto.ModuleCurriculumDto;
import com.edu.lms.course.entity.Course;
import com.edu.lms.module.entity.CourseModule;
import com.edu.lms.lesson.entity.Lesson;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.enrollment.entity.EnrollmentStatus;
import com.edu.lms.enrollment.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.edu.lms.user.entity.User;   // ← add this line

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurriculumServiceImpl implements CurriculumService {

    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final VideoUrlConverter videoUrlConverter;

    @Override
    @Transactional(readOnly = true)
    public CurriculumDto getCurriculum(UUID courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        // Check if current caller is an enrolled student
        boolean enrolled = getCurrentUserId()
                .map(userId -> enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                        userId, courseId, EnrollmentStatus.ACTIVE
                ))
                .orElse(false); // unauthenticated → not enrolled

        List<ModuleCurriculumDto> modules = course.getModules().stream()
                .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .map(module -> buildModuleDto(module, enrolled))
                .toList();

        return CurriculumDto.builder()
                .courseTitle(course.getTitle())
                .enrolled(enrolled)
                .modules(modules)
                .build();
    }

    private ModuleCurriculumDto buildModuleDto(CourseModule module, boolean enrolled) {
        List<LessonCurriculumDto> lessons = module.getLessons().stream()
                .sorted((a, b) -> Integer.compare(a.getOrderIndex(), b.getOrderIndex()))
                .map(lesson -> buildLessonDto(lesson, enrolled))
                .toList();

        return ModuleCurriculumDto.builder()
                .id(module.getId())
                .title(module.getTitle())
                .description(module.getDescription())
                .orderIndex(module.getOrderIndex())
                .lessons(lessons)
                .build();
    }

    private LessonCurriculumDto buildLessonDto(Lesson lesson, boolean enrolled) {
        // A lesson is accessible if: it's a free preview OR the user is enrolled
        boolean accessible = Boolean.TRUE.equals(lesson.getFreePreview()) || enrolled;

        return LessonCurriculumDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lesson.getType())
                .durationMinutes(lesson.getDurationMinutes())
                .orderIndex(lesson.getOrderIndex())
                .freePreview(lesson.getFreePreview())
                .accessible(accessible)
                // only pass the embed URL if accessible — locked lessons get null
                .videoUrl(accessible ? videoUrlConverter.toEmbedUrl(lesson.getVideoUrl()) : null)
                .build();
    }

    // Pull the logged-in user's ID from the security context.
    // Returns empty if the request is anonymous.
    private Optional<UUID> getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }

        if (auth.getPrincipal() instanceof User user) {
            return Optional.of(user.getId());
        }

        return Optional.empty();
    }
}
