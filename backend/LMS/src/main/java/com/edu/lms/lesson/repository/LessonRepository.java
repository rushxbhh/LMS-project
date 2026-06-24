package com.edu.lms.lesson.repository;

import com.edu.lms.lesson.entity.Lesson;
import com.edu.lms.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LessonRepository
        extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModuleIdOrderByOrderIndex(UUID moduleId);

    // LessonRepository.java
    @Query("""
    SELECT COALESCE(COUNT(l), 0), COALESCE(SUM(l.durationMinutes), 0)
    FROM Lesson l
    WHERE l.module.course.id = :courseId
    """)
    Object[] aggregateByCourseId(@Param("courseId") UUID courseId);


    @Query("""
    SELECT q FROM Quiz q
    LEFT JOIN FETCH q.lesson l
    LEFT JOIN FETCH l.module m
    LEFT JOIN FETCH m.course
    WHERE q.id = :id
    """)
    Optional<Quiz> findWithLessonById(@Param("id") UUID id);
}