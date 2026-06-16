package com.edu.lms.quiz.controller;

import com.edu.lms.common.response.ApiResponse;
import com.edu.lms.quiz.dto.*;
import com.edu.lms.quiz.service.QuizService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz", description = "Quiz management and student attempts")
public class QuizController {

    private final QuizService quizService;

    // ── Quiz CRUD ────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Create a quiz (optionally linked to a QUIZ-type lesson)")
    public ResponseEntity<ApiResponse<QuizDto>> createQuiz(
            @Valid @RequestBody CreateQuizRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Quiz created", quizService.createQuiz(request)));
    }

    @GetMapping("/{quizId}")
    @Operation(summary = "Get quiz by ID")
    public ResponseEntity<ApiResponse<QuizDto>> getQuiz(@PathVariable UUID quizId) {
        return ResponseEntity.ok(
                ApiResponse.success("Quiz fetched", quizService.getQuizById(quizId)));
    }

    @GetMapping("/by-lesson/{lessonId}")
    @Operation(summary = "Get quiz attached to a lesson")
    public ResponseEntity<ApiResponse<QuizDto>> getQuizByLesson(@PathVariable UUID lessonId) {
        return ResponseEntity.ok(
                ApiResponse.success("Quiz fetched", quizService.getQuizByLessonId(lessonId)));
    }

    @PutMapping("/{quizId}")
    @Operation(summary = "Update quiz metadata")
    public ResponseEntity<ApiResponse<QuizDto>> updateQuiz(
            @PathVariable UUID quizId,
            @Valid @RequestBody UpdateQuizRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Quiz updated", quizService.updateQuiz(quizId, request)));
    }

    @DeleteMapping("/{quizId}")
    @Operation(summary = "Delete quiz and all its questions")
    public ResponseEntity<ApiResponse<String>> deleteQuiz(@PathVariable UUID quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.ok(ApiResponse.success("Quiz deleted", "SUCCESS"));
    }

    @PostMapping("/{quizId}/publish")
    @Operation(summary = "Publish quiz — must have at least one question")
    public ResponseEntity<ApiResponse<QuizDto>> publishQuiz(@PathVariable UUID quizId) {
        return ResponseEntity.ok(
                ApiResponse.success("Quiz published", quizService.publishQuiz(quizId)));
    }

    // ── Questions ────────────────────────────────────────────────────────

    @PostMapping("/{quizId}/questions")
    @Operation(summary = "Add a question to a quiz")
    public ResponseEntity<ApiResponse<QuestionDto>> addQuestion(
            @PathVariable UUID quizId,
            @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Question added", quizService.addQuestion(quizId, request)));
    }

    @GetMapping("/{quizId}/questions")
    @Operation(summary = "List all questions for a quiz")
    public ResponseEntity<ApiResponse<List<QuestionDto>>> getQuestions(@PathVariable UUID quizId) {
        return ResponseEntity.ok(
                ApiResponse.success("Questions fetched", quizService.getQuestions(quizId)));
    }

    @PutMapping("/questions/{questionId}")
    @Operation(summary = "Update a question (replaces all options)")
    public ResponseEntity<ApiResponse<QuestionDto>> updateQuestion(
            @PathVariable UUID questionId,
            @Valid @RequestBody CreateQuestionRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Question updated",
                        quizService.updateQuestion(questionId, request)));
    }

    @DeleteMapping("/questions/{questionId}")
    @Operation(summary = "Delete a question")
    public ResponseEntity<ApiResponse<String>> deleteQuestion(@PathVariable UUID questionId) {
        quizService.deleteQuestion(questionId);
        return ResponseEntity.ok(ApiResponse.success("Question deleted", "SUCCESS"));
    }

    // ── Student attempts ─────────────────────────────────────────────────

    @PostMapping("/{quizId}/attempt")
    @Operation(summary = "Submit a quiz attempt — returns score + per-question feedback")
    public ResponseEntity<ApiResponse<AttemptResultDto>> submitAttempt(
            @PathVariable UUID quizId,
            @RequestBody SubmitAttemptRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Attempt submitted",
                        quizService.submitAttempt(quizId, request)));
    }

    @GetMapping("/{quizId}/my-attempts")
    @Operation(summary = "Get current user's attempt history for a quiz")
    public ResponseEntity<ApiResponse<List<AttemptResultDto>>> getMyAttempts(
            @PathVariable UUID quizId) {
        return ResponseEntity.ok(
                ApiResponse.success("Attempts fetched", quizService.getMyAttempts(quizId)));
    }
}
