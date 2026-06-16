package com.edu.lms.quiz.service;

import com.edu.lms.common.exception.BusinessException;
import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.lesson.entity.Lesson;
import com.edu.lms.lesson.entity.LessonType;
import com.edu.lms.lesson.repository.LessonRepository;
import com.edu.lms.enrollment.entity.EnrollmentStatus;
import com.edu.lms.enrollment.repository.EnrollmentRepository;
import com.edu.lms.quiz.dto.*;
import com.edu.lms.quiz.dto.AttemptResultDto.QuestionResultDto;
import com.edu.lms.quiz.entity.*;
import com.edu.lms.quiz.entity.Question.QuestionType;
import com.edu.lms.quiz.repository.*;
import com.edu.lms.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements QuizService {

    private final QuizRepository         quizRepository;
    private final QuestionRepository     questionRepository;
    private final QuizAttemptRepository  attemptRepository;
    private final LessonRepository       lessonRepository;
    private final EnrollmentRepository   enrollmentRepository;

    // ── Quiz CRUD ─────────────────────────────────────────────────────────

    @Override
    @Transactional
    public QuizDto createQuiz(CreateQuizRequest req) {
        Quiz.QuizBuilder builder = Quiz.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .timeLimitMinutes(req.getTimeLimitMinutes())
                .maxAttempts(req.getMaxAttempts())
                .shuffleQuestions(Boolean.TRUE.equals(req.getShuffleQuestions()));

        if (req.getPassingScore() != null) builder.passingScore(req.getPassingScore());

        if (req.getLessonId() != null) {
            Lesson lesson = lessonRepository.findById(req.getLessonId())
                    .orElseThrow(() -> new ResourceNotFoundException("Lesson not found"));
            if (lesson.getType() != LessonType.QUIZ)
                throw new BusinessException("Lesson type must be QUIZ to attach a quiz");
            if (quizRepository.existsByLessonId(req.getLessonId()))
                throw new BusinessException("A quiz is already attached to this lesson");
            builder.lesson(lesson);
        }

        return mapToDto(quizRepository.save(builder.build()));
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDto getQuizById(UUID quizId) {
        return mapToDto(findQuiz(quizId));
    }

    @Override
    @Transactional(readOnly = true)
    public QuizDto getQuizByLessonId(UUID lessonId) {
        return mapToDto(quizRepository.findByLessonId(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("No quiz found for this lesson")));
    }

    @Override
    @Transactional
    public QuizDto updateQuiz(UUID quizId, UpdateQuizRequest req) {
        Quiz quiz = findQuiz(quizId);
        if (req.getTitle()            != null) quiz.setTitle(req.getTitle());
        if (req.getDescription()      != null) quiz.setDescription(req.getDescription());
        if (req.getTimeLimitMinutes() != null) quiz.setTimeLimitMinutes(req.getTimeLimitMinutes());
        if (req.getMaxAttempts()      != null) quiz.setMaxAttempts(req.getMaxAttempts());
        if (req.getPassingScore()     != null) quiz.setPassingScore(req.getPassingScore());
        if (req.getShuffleQuestions() != null) quiz.setShuffleQuestions(req.getShuffleQuestions());
        return mapToDto(quizRepository.save(quiz));
    }

    @Override
    @Transactional
    public void deleteQuiz(UUID quizId) {
        quizRepository.delete(findQuiz(quizId));
    }

    @Override
    @Transactional
    public QuizDto publishQuiz(UUID quizId) {
        Quiz quiz = findQuiz(quizId);
        if (quiz.getQuestions().isEmpty())
            throw new BusinessException("Cannot publish a quiz with no questions");
        quiz.setPublished(true);
        return mapToDto(quizRepository.save(quiz));
    }

    // ── Question CRUD ─────────────────────────────────────────────────────

    @Override
    @Transactional
    public QuestionDto addQuestion(UUID quizId, CreateQuestionRequest req) {
        Quiz quiz = findQuiz(quizId);
        validateOptions(req);

        Question question = Question.builder()
                .text(req.getText())
                .type(req.getType())
                .points(req.getPoints() != null ? req.getPoints() : 1)
                .orderIndex(req.getOrderIndex())
                .explanation(req.getExplanation())
                .quiz(quiz)
                .options(buildOptions(req))
                .build();

        return mapToQuestionDto(questionRepository.save(question));
    }

    @Override
    @Transactional
    public QuestionDto updateQuestion(UUID questionId, CreateQuestionRequest req) {
        Question q = findQuestion(questionId);
        validateOptions(req);
        q.setText(req.getText());
        q.setType(req.getType());
        if (req.getPoints()     != null) q.setPoints(req.getPoints());
        if (req.getOrderIndex() != null) q.setOrderIndex(req.getOrderIndex());
        q.setExplanation(req.getExplanation());
        q.getOptions().clear();
        q.getOptions().addAll(buildOptions(req));
        return mapToQuestionDto(questionRepository.save(q));
    }

    @Override
    @Transactional
    public void deleteQuestion(UUID questionId) {
        questionRepository.delete(findQuestion(questionId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<QuestionDto> getQuestions(UUID quizId) {
        return findQuiz(quizId).getQuestions().stream()
                .sorted(Comparator.comparingInt(q -> q.getOrderIndex() == null ? 999 : q.getOrderIndex()))
                .map(this::mapToQuestionDto)
                .toList();
    }

    // ── Student attempts ──────────────────────────────────────────────────

    @Override
    @Transactional
    public AttemptResultDto submitAttempt(UUID quizId, SubmitAttemptRequest req) {
        User  student = requireCurrentUser();
        Quiz  quiz    = findQuiz(quizId);

        if (!quiz.getPublished())
            throw new BusinessException("This quiz is not published yet");

        // Enrollment check (only if quiz is linked to a lesson/course)
        if (quiz.getLesson() != null) {
            UUID courseId = quiz.getLesson().getModule().getCourse().getId();
            boolean enrolled = enrollmentRepository.existsByStudentIdAndCourseIdAndStatus(
                    student.getId(), courseId, EnrollmentStatus.ACTIVE);
            if (!enrolled)
                throw new AccessDeniedException("You must be enrolled to attempt this quiz");
        }

        // Attempt-limit check
        if (quiz.getMaxAttempts() != null) {
            long past = attemptRepository.countByStudentIdAndQuizId(student.getId(), quizId);
            if (past >= quiz.getMaxAttempts())
                throw new BusinessException(
                        "You have used all " + quiz.getMaxAttempts() + " attempt(s) for this quiz");
        }

        // Grade
        Map<UUID, List<Integer>> answers =
                req.getAnswers() != null ? req.getAnswers() : Collections.emptyMap();

        int totalEarned = 0, totalAvailable = 0;
        List<QuestionResultDto> results = new ArrayList<>();

        for (Question q : quiz.getQuestions()) {
            List<QuestionOption> opts    = q.getOptions();
            List<Integer>        chosen  = answers.getOrDefault(q.getId(), Collections.emptyList());

            List<Integer> correctIdx = IntStream.range(0, opts.size())
                    .filter(i -> Boolean.TRUE.equals(opts.get(i).getCorrect()))
                    .boxed().toList();

            boolean correct = new HashSet<>(chosen).equals(new HashSet<>(correctIdx));
            int earned = correct ? q.getPoints() : 0;
            totalEarned     += earned;
            totalAvailable  += q.getPoints();

            results.add(QuestionResultDto.builder()
                    .questionId(q.getId())
                    .questionText(q.getText())
                    .chosenIndices(chosen)
                    .correctIndices(correctIdx)
                    .correct(correct)
                    .pointsEarned(earned)
                    .pointsAvailable(q.getPoints())
                    .explanation(q.getExplanation())
                    .build());
        }

        int     pct    = totalAvailable == 0 ? 0 :
                (int) Math.round((double) totalEarned / totalAvailable * 100);
        boolean passed = pct >= quiz.getPassingScore();

        QuizAttempt attempt = attemptRepository.save(QuizAttempt.builder()
                .quiz(quiz)
                .student(student)
                .score(totalEarned)
                .maxScore(totalAvailable)
                .percentage(pct)
                .passed(passed)
                .timeTakenSeconds(req.getTimeTakenSeconds())
                .status(QuizAttempt.AttemptStatus.COMPLETED)
                .completedAt(LocalDateTime.now())
                .build());

        return AttemptResultDto.builder()
                .attemptId(attempt.getId())
                .quizId(quiz.getId())
                .quizTitle(quiz.getTitle())
                .score(totalEarned)
                .maxScore(totalAvailable)
                .percentage(pct)
                .passed(passed)
                .timeTakenSeconds(attempt.getTimeTakenSeconds())
                .completedAt(attempt.getCompletedAt())
                .questionResults(results)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttemptResultDto> getMyAttempts(UUID quizId) {
        User student = requireCurrentUser();
        findQuiz(quizId);
        return attemptRepository
                .findByStudentIdAndQuizIdOrderByStartedAtDesc(student.getId(), quizId)
                .stream()
                .map(a -> AttemptResultDto.builder()
                        .attemptId(a.getId())
                        .quizId(a.getQuiz().getId())
                        .quizTitle(a.getQuiz().getTitle())
                        .score(a.getScore())
                        .maxScore(a.getMaxScore())
                        .percentage(a.getPercentage())
                        .passed(a.getPassed())
                        .timeTakenSeconds(a.getTimeTakenSeconds())
                        .completedAt(a.getCompletedAt())
                        .questionResults(Collections.emptyList())
                        .build())
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private Quiz findQuiz(UUID id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz not found"));
    }

    private Question findQuestion(UUID id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    }

    private User requireCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal()))
            throw new AccessDeniedException("Authentication required");
        if (auth.getPrincipal() instanceof User u) return u;
        throw new AccessDeniedException("Authentication required");
    }

    private void validateOptions(CreateQuestionRequest req) {
        if (req.getType() == QuestionType.TRUE_FALSE) return; // auto-generated

        if (req.getOptions() == null || req.getOptions().size() < 2)
            throw new BusinessException("At least 2 options are required");

        long correctCount = req.getOptions().stream()
                .filter(o -> Boolean.TRUE.equals(o.getCorrect())).count();

        if (correctCount == 0)
            throw new BusinessException("At least one option must be marked correct");
        if (req.getType() == QuestionType.SINGLE_CHOICE && correctCount > 1)
            throw new BusinessException("SINGLE_CHOICE questions must have exactly one correct answer");
    }

    private List<QuestionOption> buildOptions(CreateQuestionRequest req) {
        if (req.getType() == QuestionType.TRUE_FALSE) {
            return List.of(
                    QuestionOption.builder().text("True").correct(true).build(),
                    QuestionOption.builder().text("False").correct(false).build()
            );
        }
        return req.getOptions().stream()
                .map(o -> QuestionOption.builder()
                        .text(o.getText())
                        .correct(Boolean.TRUE.equals(o.getCorrect()))
                        .build())
                .toList();
    }

    private QuizDto mapToDto(Quiz quiz) {
        int totalPoints = quiz.getQuestions().stream().mapToInt(Question::getPoints).sum();
        return QuizDto.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .timeLimitMinutes(quiz.getTimeLimitMinutes())
                .maxAttempts(quiz.getMaxAttempts())
                .passingScore(quiz.getPassingScore())
                .shuffleQuestions(quiz.getShuffleQuestions())
                .published(quiz.getPublished())
                .lessonId(quiz.getLesson() != null ? quiz.getLesson().getId() : null)
                .questionCount(quiz.getQuestions().size())
                .totalPoints(totalPoints)
                .build();
    }

    private QuestionDto mapToQuestionDto(Question q) {
        return QuestionDto.builder()
                .id(q.getId())
                .text(q.getText())
                .type(q.getType())
                .points(q.getPoints())
                .orderIndex(q.getOrderIndex())
                .explanation(q.getExplanation())
                .options(q.getOptions().stream()
                        .map(o -> QuestionDto.OptionDto.builder()
                                .text(o.getText())
                                .correct(o.getCorrect())
                                .build())
                        .toList())
                .build();
    }
}