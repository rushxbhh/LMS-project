package com.edu.lms.progress.repository;

import com.edu.lms.course.entity.Course;
import com.edu.lms.progress.entity.CourseProgress;
import com.edu.lms.user.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CourseProgressRepository
        extends JpaRepository<CourseProgress, UUID> {

    Optional<CourseProgress> findByStudentAndCourse(
            User student,
            Course course
    );

    @Query("""
        SELECT cp FROM CourseProgress cp
        WHERE cp.student = :student AND cp.completed = false AND cp.lastAccessedAt IS NOT NULL
        ORDER BY cp.lastAccessedAt DESC
        """)
    List<CourseProgress> findContinueLearning(@Param("student") User student, Pageable pageable);

}