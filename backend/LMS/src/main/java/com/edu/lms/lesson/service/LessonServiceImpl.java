package com.edu.lms.lesson.service;

import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.lesson.dto.CreateLessonRequest;
import com.edu.lms.lesson.dto.LessonDto;
import com.edu.lms.lesson.dto.UpdateLessonRequest;
import com.edu.lms.course.entity.Course;
import com.edu.lms.module.entity.CourseModule;
import com.edu.lms.lesson.entity.Lesson;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.lesson.repository.LessonRepository;
import com.edu.lms.module.repository.ModuleRepository;
import com.edu.lms.enrollment.entity.EnrollmentStatus;
import com.edu.lms.enrollment.repository.EnrollmentRepository;
import com.edu.lms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final EnrollmentRepository enrollmentRepository;
    private final LessonRepository     lessonRepository;
    private final ModuleRepository     moduleRepository;
    private final CourseRepository     courseRepository;

    // ── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LessonDto createLesson(UUID moduleId, CreateLessonRequest request) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // Ownership: only teacher who owns the course or ADMIN may add lessons
        requireCourseOwnerOrAdmin(module.getCourse());

        Lesson lesson = Lesson.builder()
                .title(request.getTitle())
                .type(request.getType())
                .videoUrl(request.getVideoUrl())
                .content(request.getContent())
                .durationMinutes(request.getDurationMinutes())
                .orderIndex(request.getOrderIndex())
                .freePreview(Boolean.TRUE.equals(request.getFreePreview()))
                .module(module)
                .build();

        lesson = lessonRepository.save(lesson);
        updateCourseStats(module.getCourse());
        return mapToDto(lesson);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LessonDto updateLesson(UUID lessonId, UpdateLessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        requireCourseOwnerOrAdmin(lesson.getModule().getCourse());

        // Null-safe: only set fields that are explicitly provided
        if (request.getTitle()           != null) lesson.setTitle(request.getTitle());
        if (request.getType()            != null) lesson.setType(request.getType());
        if (request.getVideoUrl()        != null) lesson.setVideoUrl(request.getVideoUrl());
        if (request.getContent()         != null) lesson.setContent(request.getContent());
        if (request.getDurationMinutes() != null) lesson.setDurationMinutes(request.getDurationMinutes());
        if (request.getOrderIndex()      != null) lesson.setOrderIndex(request.getOrderIndex());
        if (request.getFreePreview()     != null) lesson.setFreePreview(request.getFreePreview());

        lesson = lessonRepository.save(lesson);
        updateCourseStats(lesson.getModule().getCourse());
        return mapToDto(lesson);
    }

    // ── Read (enrollment-gated) ───────────────────────────────────────────────
    // FIX: @Transactional prevents LazyInitializationException when traversing
    // lesson → module → course inside a detached state.

    @Override
    @Transactional(readOnly = true)
    public LessonDto getLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        if (Boolean.TRUE.equals(lesson.getFreePreview())) {
            return mapToDto(lesson);
        }

        // Locked — must be authenticated and enrolled
        UUID currentUserId = getCurrentUserId()
                .orElseThrow(() -> new AccessDeniedException("Login required to access this lesson"));

        UUID courseId = lesson.getModule().getCourse().getId();

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                currentUserId, courseId, EnrollmentStatus.ACTIVE);

        if (!enrolled) {
            throw new AccessDeniedException("Enroll in this course to access the lesson");
        }

        return mapToDto(lesson);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteLesson(UUID lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));

        requireCourseOwnerOrAdmin(lesson.getModule().getCourse());

        Course course = lesson.getModule().getCourse();
        lessonRepository.delete(lesson);
        updateCourseStats(course);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void requireCourseOwnerOrAdmin(Course course) {
        User caller = currentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
        if (caller.getRole() == User.Role.ADMIN) return;
        if (course.getTeacher() == null
                || !course.getTeacher().getId().equals(caller.getId())) {
            throw new AccessDeniedException("You do not own this course");
        }
    }

    private void updateCourseStats(Course course) {
        Object[] result = lessonRepository.aggregateByCourseId(course.getId());
        course.setTotalLessons(((Long) result[0]).intValue());
        course.setTotalDurationMinutes(result[1] != null ? ((Long) result[1]).intValue() : 0);
        courseRepository.save(course);
    }

    private Optional<UUID> getCurrentUserId() {
        return currentUser().map(User::getId);
    }

    private Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof User u) return Optional.of(u);
        return Optional.empty();
    }

    private LessonDto mapToDto(Lesson lesson) {
        return LessonDto.builder()
                .id(lesson.getId())
                .title(lesson.getTitle())
                .type(lesson.getType())
                .videoUrl(lesson.getVideoUrl())
                .content(lesson.getContent())
                .durationMinutes(lesson.getDurationMinutes())
                .orderIndex(lesson.getOrderIndex())
                .freePreview(lesson.getFreePreview())
                .build();
    }
}