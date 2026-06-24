package com.edu.lms.course.service;

import com.edu.lms.common.exception.BusinessException;
import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.course.dto.CourseDto;
import com.edu.lms.course.dto.CreateCourseRequest;
import com.edu.lms.course.dto.UpdateCourseRequest;
import com.edu.lms.course.entity.Course;
import com.edu.lms.course.entity.CourseStatus;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.user.entity.User;
import com.edu.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository   userRepository;

    // ── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CourseDto createCourse(CreateCourseRequest request, User teacher) {
        teacher = userRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));

        // Only TEACHER or ADMIN roles should be allowed; enforce ownership intent
        User caller = currentUser().orElse(null);
           if (caller != null && caller.getRole() == User.Role.STUDENT) {
            throw new AccessDeniedException("Students cannot create courses");
       }

        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .thumbnailUrl(request.getThumbnailUrl())
                .price(request.getPrice())
                .isFree(request.getIsFree())
                .level(request.getLevel())
                .category(request.getCategory())
                .status(CourseStatus.DRAFT)
                .teacher(teacher)
                .build();

        return mapToDto(courseRepository.save(course));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CourseDto> getAllPublishedCourses() {
        return courseRepository.findByStatus(CourseStatus.PUBLISHED)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public CourseDto getCourseById(UUID id) {
        return mapToDto(courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found")));
    }

    // ── Update (null-safe PATCH semantics) ───────────────────────────────────

    @Override
    @Transactional
    public CourseDto updateCourse(UUID id, UpdateCourseRequest request) {
        Course course = findAndCheckOwnership(id);

        // Only update fields that were actually sent (non-null)
        if (request.getTitle()        != null) course.setTitle(request.getTitle());
        if (request.getDescription()  != null) course.setDescription(request.getDescription());
        if (request.getThumbnailUrl() != null) course.setThumbnailUrl(request.getThumbnailUrl());
        if (request.getPrice()        != null) course.setPrice(request.getPrice());
        if (request.getIsFree()       != null) course.setIsFree(request.getIsFree());
        if (request.getCategory()     != null) course.setCategory(request.getCategory());
        if (request.getLevel()        != null) course.setLevel(request.getLevel());

        return mapToDto(courseRepository.save(course));
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteCourse(UUID id) {
        courseRepository.delete(findAndCheckOwnership(id));
    }

    // ── Publish ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CourseDto publishCourse(UUID id) {
    Course course = findAndCheckOwnership(id);

    User caller = currentUser()
            .orElseThrow(() -> new AccessDeniedException("Authentication required"));

    if (caller.getRole() == User.Role.TEACHER
            && caller.getInstructorApplicationStatus() != User.InstructorApplicationStatus.APPROVED) {
        throw new AccessDeniedException(
                "Your instructor account isn't verified yet. Submit a verification application to unlock publishing.");
    }

    if (course.getStatus() == CourseStatus.PENDING_REVIEW) {
        throw new BusinessException("This course is currently under instructor review");
    }
    if (course.getStatus() == CourseStatus.PUBLISHED) {
        throw new BusinessException("Course is already published");
    }

    boolean hasLesson = course.getModules().stream()
            .anyMatch(m -> !m.getLessons().isEmpty());
    if (!hasLesson) {
        throw new BusinessException(
                "Cannot publish a course with no lessons. Add at least one module with a lesson first.");
    }

    course.setStatus(CourseStatus.PUBLISHED);
    return mapToDto(courseRepository.save(course));
    }

    // ── Ownership helper ─────────────────────────────────────────────────────

    /**
     * Loads the course and verifies the caller is the owner (TEACHER)
     * or an ADMIN. Throws 404 / 403 appropriately.
     */
    private Course findAndCheckOwnership(UUID courseId) {
        Course course = courseRepository.findWithModulesAndLessonsById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        User caller = currentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));

        if (caller.getRole() == User.Role.ADMIN) return course; // admins bypass ownership

        if (course.getTeacher() == null
                || !course.getTeacher().getId().equals(caller.getId())) {
            throw new AccessDeniedException("You do not own this course");
        }

        return course;
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

    // ── Mapper ───────────────────────────────────────────────────────────────

    private CourseDto mapToDto(Course course) {
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .thumbnailUrl(course.getThumbnailUrl())
                .price(course.getPrice())
                .isFree(course.getIsFree())
                .level(course.getLevel())
                .category(course.getCategory())
                .status(course.getStatus())
                .totalLessons(course.getTotalLessons())
                .totalDurationMinutes(course.getTotalDurationMinutes())
                .build();
    }
}