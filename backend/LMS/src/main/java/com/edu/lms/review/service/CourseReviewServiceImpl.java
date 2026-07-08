package com.edu.lms.review.service;

import com.edu.lms.common.exception.BusinessException;
import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.course.entity.Course;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.enrollment.entity.EnrollmentStatus;
import com.edu.lms.enrollment.repository.EnrollmentRepository;
import com.edu.lms.review.dto.CourseReviewDto;
import com.edu.lms.review.dto.CreateReviewRequest;
import com.edu.lms.review.entity.CourseReview;
import com.edu.lms.review.repository.CourseReviewRepository;
import com.edu.lms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static com.edu.lms.config.RedisConfig.CACHE_COURSE;
import static com.edu.lms.config.RedisConfig.CACHE_COURSES;

@Service
@RequiredArgsConstructor
public class CourseReviewServiceImpl implements CourseReviewService {

    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    // ── Create / update — one review per student per course, upsert ────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_COURSES, key = "'all'"),
            @CacheEvict(cacheNames = CACHE_COURSE,  key = "#courseId")
    })
    public CourseReviewDto submitReview(UUID courseId, CreateReviewRequest request) {
        User student = currentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found"));

        boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                student.getId(), courseId, EnrollmentStatus.ACTIVE);
        if (!enrolled) {
            throw new BusinessException("Only enrolled students can review this course");
        }

        CourseReview review = reviewRepository.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseGet(() -> CourseReview.builder().student(student).course(course).build());

        review.setRating(round1(request.getRating()));
        review.setComment(request.getComment());
        review = reviewRepository.save(review);

        recalculateCourseRating(course);
        return mapToDto(review);
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CACHE_COURSES, key = "'all'"),
            @CacheEvict(cacheNames = CACHE_COURSE,  key = "#courseId")
    })
    public void deleteReview(UUID courseId, UUID reviewId) {
        User caller = currentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));

        CourseReview review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found"));

        if (!review.getCourse().getId().equals(courseId)) {
            throw new BusinessException("Review does not belong to the specified course");
        }
        if (caller.getRole() != User.Role.ADMIN && !review.getStudent().getId().equals(caller.getId())) {
            throw new AccessDeniedException("You can only delete your own review");
        }

        Course course = review.getCourse();
        reviewRepository.delete(review);
        recalculateCourseRating(course);
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public Page<CourseReviewDto> getCourseReviews(UUID courseId, Pageable pageable) {
        if (!courseRepository.existsById(courseId)) {
            throw new ResourceNotFoundException("Course not found");
        }
        return reviewRepository.findByCourseIdOrderByCreatedAtDesc(courseId, pageable).map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CourseReviewDto> getMyReview(UUID courseId) {
        User student = currentUser()
                .orElseThrow(() -> new AccessDeniedException("Authentication required"));
        return reviewRepository.findByStudentIdAndCourseId(student.getId(), courseId).map(this::mapToDto);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void recalculateCourseRating(Course course) {
        Double avg = reviewRepository.findAverageRatingByCourseId(course.getId());
        long count = reviewRepository.countByCourseId(course.getId());
        course.setAverageRating(avg != null ? round1(avg) : 0.0);
        course.setReviewCount((int) count);
        courseRepository.save(course);
    }

    private Double round1(Double value) {
        return value == null ? null : Math.round(value * 10) / 10.0;
    }

    private Optional<User> currentUser() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return Optional.empty();
        }
        if (auth.getPrincipal() instanceof User u) return Optional.of(u);
        return Optional.empty();
    }

    private CourseReviewDto mapToDto(CourseReview review) {
        return CourseReviewDto.builder()
                .id(review.getId())
                .studentId(review.getStudent().getId())
                .studentName(review.getStudent().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}