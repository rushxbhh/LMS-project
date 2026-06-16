package com.edu.lms.quiz.repository;

import com.edu.lms.quiz.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
    Optional<Quiz> findByLessonId(UUID lessonId);
    boolean existsByLessonId(UUID lessonId);
}