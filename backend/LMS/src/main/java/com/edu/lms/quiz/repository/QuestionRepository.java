package com.edu.lms.quiz.repository;

import com.edu.lms.quiz.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {
    List<Question> findByQuizIdOrderByOrderIndex(UUID quizId);
}