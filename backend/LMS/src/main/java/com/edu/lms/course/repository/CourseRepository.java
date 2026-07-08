package com.edu.lms.course.repository;

import com.edu.lms.course.dto.CourseEnrollmentSummaryDto;
import com.edu.lms.course.entity.Course;
import com.edu.lms.course.entity.CourseLevel;
import com.edu.lms.course.entity.CourseStatus;
import com.edu.lms.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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


    // NEW
    List<Course> findByTeacherOrderByCreatedAtDesc(User teacher);

    // Optional (use later for filters)
    List<Course> findByTeacherAndStatusOrderByCreatedAtDesc(
            User teacher,
            CourseStatus status
    );


    long countByStatus(CourseStatus status);

    @Query("""
    SELECT new com.edu.lms.course.dto.CourseEnrollmentSummaryDto(
        c.id, c.title, c.status,
        SUM(CASE WHEN e.status = com.edu.lms.enrollment.entity.EnrollmentStatus.ACTIVE THEN 1L ELSE 0L END))
    FROM Course c LEFT JOIN c.enrollments e
    GROUP BY c.id, c.title, c.status
    """)
    List<CourseEnrollmentSummaryDto> findEnrollmentSummary();



    long countByTeacher(User teacher);

    long countByTeacherAndStatus(
            User teacher,
            CourseStatus status
    );

    long countByTeacherAndIsFree(
            User teacher,
            Boolean isFree
    );


    @Modifying
    @Query("UPDATE Course c SET c.enrolledCount = COALESCE(c.enrolledCount, 0) + 1 WHERE c.id = :courseId")
    void incrementEnrolledCount(@Param("courseId") UUID courseId);

    @Query("""
        SELECT c FROM Course c
        WHERE c.status = com.edu.lms.course.entity.CourseStatus.PUBLISHED
          AND (:search IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :search, '%'))
                                OR LOWER(c.category) LIKE LOWER(CONCAT('%', :search, '%')))
          AND (:category IS NULL OR c.category = :category)
          AND (:level IS NULL OR c.level = :level)
          AND (:minPrice IS NULL OR c.price >= :minPrice)
          AND (:maxPrice IS NULL OR c.price <= :maxPrice)
        """)
    Page<Course> searchCourses(@Param("search") String search,
                               @Param("category") String category,
                               @Param("level") CourseLevel level,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               Pageable pageable);
}