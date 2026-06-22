package com.edu.lms.module.service;

import com.edu.lms.common.exception.BusinessException;
import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.module.dto.CreateModuleRequest;
import com.edu.lms.module.dto.ModuleDto;
import com.edu.lms.module.dto.UpdateModuleRequest;
import com.edu.lms.course.entity.Course;
import com.edu.lms.module.entity.CourseModule;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.module.repository.ModuleRepository;
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
public class ModuleServiceImpl implements ModuleService {

    private final CourseRepository  courseRepository;
    private final ModuleRepository  moduleRepository;

    // ── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public ModuleDto createModule(UUID courseId, CreateModuleRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        requireCourseOwnerOrAdmin(course);

        CourseModule module = CourseModule.builder()
                .title(request.getTitle())
                .orderIndex(request.getOrderIndex())
                .course(course)
                .build();

        return mapToDto(moduleRepository.save(module));
    }

    // ── Update ───────────────────────────────────────────────────────────────
    // FIX: courseId in path is now validated against the module's actual course.

    @Override
    @Transactional
    public ModuleDto updateModule(UUID courseId, UUID moduleId, UpdateModuleRequest request) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        // Validate courseId matches module's actual course (prevent path spoofing)
        if (!module.getCourse().getId().equals(courseId)) {
            throw new BusinessException("Module does not belong to the specified course");
        }

        requireCourseOwnerOrAdmin(module.getCourse());

        if (request.getTitle()      != null) module.setTitle(request.getTitle());
        if (request.getOrderIndex() != null) module.setOrderIndex(request.getOrderIndex());

        return mapToDto(moduleRepository.save(module));
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteModule(UUID courseId, UUID moduleId) {
        CourseModule module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getCourse().getId().equals(courseId)) {
            throw new BusinessException("Module does not belong to the specified course");
        }

        requireCourseOwnerOrAdmin(module.getCourse());
        moduleRepository.delete(module);
    }

    // ── Ownership helper ─────────────────────────────────────────────────────

    private void requireCourseOwnerOrAdmin(Course course) {
        User caller = currentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
        if (caller.getRole() == User.Role.ADMIN) return;
        if (course.getTeacher() == null
                || !course.getTeacher().getId().equals(caller.getId())) {
            throw new AccessDeniedException("You do not own this course");
        }
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

    private ModuleDto mapToDto(CourseModule module) {
        return ModuleDto.builder()
                .id(module.getId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .build();
    }
}