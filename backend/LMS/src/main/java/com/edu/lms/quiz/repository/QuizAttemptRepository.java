package com.edu.lms.quiz.repository;

import com.edu.lms.quiz.entity.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, UUID> {
    long countByStudentIdAndQuizId(UUID studentId, UUID quizId);
    List<QuizAttempt> findByStudentIdAndQuizIdOrderByStartedAtDesc(UUID studentId, UUID quizId);
}