package com.edu.lms.progress.repository;

import com.edu.lms.lesson.entity.Lesson;
import com.edu.lms.progress.entity.LessonProgress;
import com.edu.lms.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LessonProgressRepository
        extends JpaRepository<LessonProgress, UUID> {

    Optional<LessonProgress> findByStudentAndLesson(
            User student,
            Lesson lesson
    );

    List<LessonProgress> findByStudent(User student);

    long countByStudentAndCompletedTrue(User student);

    long countByStudentAndLesson_Course_IdAndCompletedTrue(
            User student,
            UUID courseId
    );
}