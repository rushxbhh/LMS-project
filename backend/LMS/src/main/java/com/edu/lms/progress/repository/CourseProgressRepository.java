package com.edu.lms.progress.repository;

import com.edu.lms.course.entity.Course;
import com.edu.lms.progress.entity.CourseProgress;
import com.edu.lms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseProgressRepository
        extends JpaRepository<CourseProgress, UUID> {

    Optional<CourseProgress> findByStudentAndCourse(
            User student,
            Course course
    );

}