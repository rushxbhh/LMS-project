package com.edu.lms.progress.service;

import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.course.entity.Course;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.enrollment.entity.EnrollmentStatus;
import com.edu.lms.enrollment.repository.EnrollmentRepository;
import com.edu.lms.lesson.entity.Lesson;
import com.edu.lms.lesson.repository.LessonRepository;
import com.edu.lms.progress.dto.CourseProgressDto;
import com.edu.lms.progress.dto.SaveProgressRequest;
import com.edu.lms.progress.entity.CourseProgress;
import com.edu.lms.progress.entity.LessonProgress;
import com.edu.lms.progress.repository.CourseProgressRepository;
import com.edu.lms.progress.repository.LessonProgressRepository;
import com.edu.lms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgressServiceImpl implements ProgressService {

    private final LessonRepository lessonRepository;
    private final CourseRepository courseRepository;
    private final LessonProgressRepository lessonProgressRepository;
    private final CourseProgressRepository courseProgressRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ── Open ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void openLesson(UUID lessonId) {
        Lesson lesson = getLessonOrThrow(lessonId);
        User student = requireEnrolledStudent(lesson);

        LessonProgress lp = findOrInitLessonProgress(student, lesson);
        lp.setLastAccessedAt(LocalDateTime.now());
        lessonProgressRepository.save(lp);

        touchCourseProgress(student, lesson, false);
    }

    // ── Save (playback position) ────────────────────────────────────────────

    @Override
    @Transactional
    public void saveProgress(UUID lessonId, SaveProgressRequest request) {
        Lesson lesson = getLessonOrThrow(lessonId);
        User student = requireEnrolledStudent(lesson);

        LessonProgress lp = findOrInitLessonProgress(student, lesson);

        // Null-safe: only update fields actually sent
        if (request.getWatchedSeconds() != null) lp.setWatchedSeconds(request.getWatchedSeconds());
        if (request.getWatchedPercentage() != null) lp.setWatchedPercentage(request.getWatchedPercentage());
        lp.setLastAccessedAt(LocalDateTime.now());
        lessonProgressRepository.save(lp);

        touchCourseProgress(student, lesson, false);
    }

    // ── Complete ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void completeLesson(UUID lessonId) {
        Lesson lesson = getLessonOrThrow(lessonId);
        User student = requireEnrolledStudent(lesson);

        LessonProgress lp = findOrInitLessonProgress(student, lesson);
        if (!lp.isCompleted()) {
            lp.setCompleted(true);
            lp.setCompletedAt(LocalDateTime.now());
        }
        lp.setWatchedPercentage(100);
        lp.setLastAccessedAt(LocalDateTime.now());
        lessonProgressRepository.save(lp);

        // Recalculate the cached course-level rollup
        touchCourseProgress(student, lesson, true);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public CourseProgressDto getCourseProgress(UUID courseId) {
        User student = currentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), courseId, EnrollmentStatus.ACTIVE);
        if (!enrolled) {
            throw new AccessDeniedException("Enroll in this course to view progress");
        }

        return courseProgressRepository.findByStudentAndCourse(student, course)
                .map(this::mapToDto)
                .orElseGet(() -> CourseProgressDto.builder()
                        .courseId(courseId)
                        .progressPercentage(0)
                        .completedLessons(0)
                        .totalLessons(course.getTotalLessons())
                        .completed(false)
                        .build());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private LessonProgress findOrInitLessonProgress(User student, Lesson lesson) {
        return lessonProgressRepository.findByStudentAndLesson(student, lesson)
                .orElseGet(() -> LessonProgress.builder()
                        .student(student)
                        .lesson(lesson)
                        .watchedSeconds(0)
                        .watchedPercentage(0)
                        .completed(false)
                        .build());
    }

    /**
     * Recomputes the cached CourseProgress row after a lesson-level change.
     * recalculate=true recounts completedLessons/percentage (call on completeLesson);
     * recalculate=false just bumps "last accessed" (call on open/save, cheaper).
     */
    private void touchCourseProgress(User student, Lesson lesson, boolean recalculate) {
        Course course = lesson.getModule().getCourse();
        CourseProgress cp = courseProgressRepository.findByStudentAndCourse(student, course)
                .orElseGet(() -> CourseProgress.builder()
                        .student(student)
                        .course(course)
                        .progressPercentage(0)
                        .completedLessons(0)
                        .completed(false)
                        .build());

        cp.setLastLesson(lesson);
        cp.setLastAccessedAt(LocalDateTime.now());

        if (recalculate) {
            int totalLessons = course.getTotalLessons() == null ? 0 : course.getTotalLessons();
            long completedCount = lessonProgressRepository
                    .countByStudentAndLesson_Course_IdAndCompletedTrue(student, course.getId());

            cp.setCompletedLessons((int) completedCount);
            cp.setProgressPercentage(totalLessons > 0
                    ? (int) Math.round((completedCount * 100.0) / totalLessons)
                    : 0);
            cp.setCompleted(totalLessons > 0 && completedCount >= totalLessons);
        }

        courseProgressRepository.save(cp);
    }

    private Lesson getLessonOrThrow(UUID lessonId) {
        return lessonRepository.findById(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
    }

    /**
     * Enrollment is required to track progress, EXCEPT free-preview lessons —
     * a logged-in guest sampling a preview can still have their spot saved.
     */
    private User requireEnrolledStudent(Lesson lesson) {
        User student = currentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));

        UUID courseId = lesson.getModule().getCourse().getId();
        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), courseId, EnrollmentStatus.ACTIVE);

        if (!enrolled && !Boolean.TRUE.equals(lesson.getFreePreview())) {
            throw new AccessDeniedException("Enroll in this course to track progress");
        }
        return student;
    }

    private Optional<User> currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof User u) return Optional.of(u);
        return Optional.empty();
    }

    private CourseProgressDto mapToDto(CourseProgress cp) {
        return CourseProgressDto.builder()
                .courseId(cp.getCourse().getId())
                .progressPercentage(cp.getProgressPercentage())
                .completedLessons(cp.getCompletedLessons())
                .totalLessons(cp.getCourse().getTotalLessons())
                .lastLessonId(cp.getLastLesson() != null ? cp.getLastLesson().getId() : null)
                .completed(cp.isCompleted())
                .build();
    }
}