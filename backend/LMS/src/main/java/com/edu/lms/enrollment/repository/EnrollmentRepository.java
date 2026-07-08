package com.edu.lms.enrollment.repository;

import com.edu.lms.enrollment.entity.Enrollment;
import com.edu.lms.enrollment.entity.EnrollmentStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);

    boolean existsByStudentIdAndCourseIdAndStatus(UUID id, UUID courseId, EnrollmentStatus enrollmentStatus);

    Page<Enrollment> findByStudentIdAndStatusOrderByEnrolledAtDesc(
            UUID studentId, EnrollmentStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Course c SET c.enrolledCount = COALESCE(c.enrolledCount, 0) + 1 WHERE c.id = :courseId")
    void incrementCourseEnrolledCount(@Param("courseId") UUID courseId);
}