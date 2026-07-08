package com.edu.lms.review.repository;

import com.edu.lms.review.entity.CourseReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CourseReviewRepository extends JpaRepository<CourseReview, UUID> {

    Optional<CourseReview> findByStudentIdAndCourseId(UUID studentId, UUID courseId);

    Page<CourseReview> findByCourseIdOrderByCreatedAtDesc(UUID courseId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM CourseReview r WHERE r.course.id = :courseId")
    Double findAverageRatingByCourseId(@Param("courseId") UUID courseId);

    long countByCourseId(UUID courseId);
}