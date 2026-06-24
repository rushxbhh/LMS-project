package com.edu.lms.instructor.service;

import com.edu.lms.common.exception.BusinessException;
import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.course.entity.Course;
import com.edu.lms.course.entity.CourseStatus;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.instructor.dto.*;
import com.edu.lms.instructor.entity.InstructorApplication;
import com.edu.lms.instructor.entity.InstructorApplicationStatus;
import com.edu.lms.instructor.entity.InstructorApplicationType;
import com.edu.lms.instructor.repository.InstructorApplicationRepository;
import com.edu.lms.user.entity.User;
import com.edu.lms.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InstructorApplicationServiceImpl implements InstructorApplicationService {

    private final InstructorApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    // ── Submit ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InstructorApplicationDto submitExperienced(UUID applicantId, SubmitExperiencedApplicationRequest request) {
        User applicant = loadUser(applicantId);
        guardCanApply(applicant);

        applicant.setInstructorApplicationStatus(User.InstructorApplicationStatus.PENDING);
        userRepository.save(applicant);

        InstructorApplication application = InstructorApplication.builder()
                .applicant(applicant)
                .type(InstructorApplicationType.EXPERIENCED)
                .status(InstructorApplicationStatus.PENDING)
                .resumeUrl(request.getResumeUrl())
                .linkedinUrl(request.getLinkedinUrl())
                .teachingExperience(request.getTeachingExperience())
                .build();

        log.info("Experienced instructor application submitted by user: {}", applicantId);
        return InstructorApplicationDto.from(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public InstructorApplicationDto submitNewbie(UUID applicantId, SubmitNewbieApplicationRequest request) {
        User applicant = loadUser(applicantId);
        guardCanApply(applicant);

        applicant.setInstructorApplicationStatus(User.InstructorApplicationStatus.PENDING);
        userRepository.save(applicant);

        Course course = courseRepository.findWithModulesAndLessonsById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        if (course.getTeacher() == null || !course.getTeacher().getId().equals(applicant.getId())) {
            throw new AccessDeniedException("You do not own this course");
        }

        if (course.getStatus() != CourseStatus.DRAFT) {
            throw new BusinessException("Only a draft course can be submitted for instructor review");
        }

        boolean hasLesson = course.getModules().stream()
                .anyMatch(m -> !m.getLessons().isEmpty());
        if (!hasLesson) {
            throw new BusinessException(
                    "Add at least one module with a lesson before submitting your course for review");
        }

        course.setStatus(CourseStatus.PENDING_REVIEW);
        courseRepository.save(course);

        InstructorApplication application = InstructorApplication.builder()
                .applicant(applicant)
                .type(InstructorApplicationType.NEWBIE)
                .status(InstructorApplicationStatus.PENDING)
                .proposedCourse(course)
                .build();

        log.info("Newbie instructor application submitted by user: {} with course: {}",
                applicantId, course.getId());
        return InstructorApplicationDto.from(applicationRepository.save(application));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InstructorApplicationDto> getMyApplications(UUID applicantId) {
        return applicationRepository.findByApplicantIdOrderByCreatedAtDesc(applicantId)
                .stream()
                .map(InstructorApplicationDto::from)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<InstructorApplicationDto> getApplicationsForAdmin(
            InstructorApplicationType type, InstructorApplicationStatus status, Pageable pageable) {

        Page<InstructorApplication> page;
        if (type != null && status != null) {
            page = applicationRepository.findByTypeAndStatus(type, status, pageable);
        } else if (type != null) {
            page = applicationRepository.findByType(type, pageable);
        } else if (status != null) {
            page = applicationRepository.findByStatus(status, pageable);
        } else {
            page = applicationRepository.findAll(pageable);
        }
        return page.map(InstructorApplicationDto::from);
    }

    // ── Review ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InstructorApplicationDto approve(UUID applicationId, UUID adminId, String note) {
        InstructorApplication application = getPendingOrThrow(applicationId);
        User admin = loadUser(adminId);

        User applicant = application.getApplicant();
        applicant.setInstructorApplicationStatus(User.InstructorApplicationStatus  .APPROVED);
        userRepository.save(applicant);

        if (application.getType() == InstructorApplicationType.NEWBIE) {
            Course course = application.getProposedCourse();
            course.setStatus(CourseStatus.PUBLISHED);
            courseRepository.save(course);
        }

        application.setStatus(InstructorApplicationStatus.APPROVED);
        application.setReviewedBy(admin);
        application.setReviewedAt(LocalDateTime.now());
        application.setAdminNote(note);

        log.info("Instructor application {} approved by admin {}", applicationId, adminId);
        return InstructorApplicationDto.from(applicationRepository.save(application));
    }

    @Override
    @Transactional
    public InstructorApplicationDto reject(UUID applicationId, UUID adminId, String reason) {
        InstructorApplication application = getPendingOrThrow(applicationId);
        User admin = loadUser(adminId);

        User applicant = application.getApplicant();
        applicant.setInstructorApplicationStatus(User.InstructorApplicationStatus.REJECTED);
        userRepository.save(applicant);

        if (application.getType() == InstructorApplicationType.NEWBIE) {
            Course course = application.getProposedCourse();
            course.setStatus(CourseStatus.DRAFT); // back to draft — they can revise and reapply
            courseRepository.save(course);
        }

        application.setStatus(InstructorApplicationStatus.REJECTED);
        application.setReviewedBy(admin);
        application.setReviewedAt(LocalDateTime.now());
        application.setAdminNote(reason);

        log.info("Instructor application {} rejected by admin {}", applicationId, adminId);
        return InstructorApplicationDto.from(applicationRepository.save(application));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private User loadUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private InstructorApplication getPendingOrThrow(UUID applicationId) {
        InstructorApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (application.getStatus() != InstructorApplicationStatus.PENDING) {
            throw new BusinessException("This application has already been reviewed");
        }
        return application;
    }


    private void guardCanApply(User applicant) {
    if (applicant.getRole() != User.Role.TEACHER) {
        throw new BusinessException("Only teacher accounts can submit a verification application");
    }
    if (applicant.getInstructorApplicationStatus() == User.InstructorApplicationStatus.APPROVED) {
        throw new BusinessException("You are already a verified instructor");
    }
    if (applicationRepository.existsByApplicantIdAndStatus(
            applicant.getId(), InstructorApplicationStatus.PENDING)) {
        throw new BusinessException("You already have a pending verification application");
    }
}
    
}
