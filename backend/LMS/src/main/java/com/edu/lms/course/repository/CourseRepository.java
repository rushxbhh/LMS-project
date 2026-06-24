package com.edu.lms.course.repository;

import com.edu.lms.course.dto.CourseEnrollmentSummaryDto;
import com.edu.lms.course.entity.Course;
import com.edu.lms.course.entity.CourseStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    List<Course> findByStatus(CourseStatus status);


    // CourseRepository.java
    @Query("""
    SELECT c FROM Course c
    LEFT JOIN FETCH c.modules m
    LEFT JOIN FETCH m.lessons
    WHERE c.id = :id
    """)
    Optional<Course> findWithModulesAndLessonsById(@Param("id") UUID id);


    long countByStatus(CourseStatus status);

    @Query("""
    SELECT new com.edu.lms.course.dto.CourseEnrollmentSummaryDto(
        c.id, c.title, c.status,
        SUM(CASE WHEN e.status = com.edu.lms.enrollment.entity.EnrollmentStatus.ACTIVE THEN 1L ELSE 0L END))
    FROM Course c LEFT JOIN c.enrollments e
    GROUP BY c.id, c.title, c.status
    """)
    List<CourseEnrollmentSummaryDto> findEnrollmentSummary();
}