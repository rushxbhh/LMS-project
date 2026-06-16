package com.edu.lms.quiz.service;

import com.edu.lms.quiz.dto.*;
import java.util.List;
import java.util.UUID;

public interface QuizService {
    // Quiz CRUD
    QuizDto createQuiz(CreateQuizRequest request);
    QuizDto getQuizById(UUID quizId);
    QuizDto getQuizByLessonId(UUID lessonId);
    QuizDto updateQuiz(UUID quizId, UpdateQuizRequest request);
    void    deleteQuiz(UUID quizId);
    QuizDto publishQuiz(UUID quizId);

    // Question CRUD
    QuestionDto        addQuestion(UUID quizId, CreateQuestionRequest request);
    QuestionDto        updateQuestion(UUID questionId, CreateQuestionRequest request);
    void               deleteQuestion(UUID questionId);
    List<QuestionDto>  getQuestions(UUID quizId);

    // Student attempts
    AttemptResultDto       submitAttempt(UUID quizId, SubmitAttemptRequest request);
    List<AttemptResultDto> getMyAttempts(UUID quizId);
}