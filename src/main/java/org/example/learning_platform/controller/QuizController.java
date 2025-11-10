package org.example.learning_platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learning_platform.dto.*;
import org.example.learning_platform.model.*;
import org.example.learning_platform.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class QuizController {
    private final QuizService quizService;

    @PostMapping("/modules/{moduleId}/quizzes")
    public ResponseEntity<QuizDTO> createQuiz(
            @PathVariable Long moduleId,
            @Valid @RequestBody QuizCreateRequest request) {
        Quiz quiz = Quiz.builder()
                .title(request.getTitle())
                .timeLimit(request.getTimeLimit())
                .passingScore(request.getPassingScore())
                .build();
        Quiz createdQuiz = quizService.createQuiz(moduleId, quiz);
        return ResponseEntity.status(HttpStatus.CREATED).body(toQuizDTO(createdQuiz));
    }

    @PostMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<QuestionDTO> addQuestion(
            @PathVariable Long quizId,
            @Valid @RequestBody QuestionCreateRequest request) {
        Question question = Question.builder()
                .text(request.getText())
                .type(Question.QuestionType.valueOf(request.getType()))
                .points(request.getPoints())
                .build();
        Question createdQuestion = quizService.addQuestion(quizId, question);
        return ResponseEntity.status(HttpStatus.CREATED).body(toQuestionDTO(createdQuestion));
    }

    @PostMapping("/quizzes/{quizId}/take")
    public ResponseEntity<QuizSubmissionDTO> takeQuiz(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizTakeRequest request) {
        QuizSubmission submission = quizService.submitQuiz(
                quizId,
                request.getStudentId(),
                request.getScore()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toQuizSubmissionDTO(submission));
    }

    @GetMapping("/students/{studentId}/quiz-results")
    public ResponseEntity<List<QuizSubmissionDTO>> getStudentQuizResults(@PathVariable Long studentId) {
        List<QuizSubmission> submissions = quizService.getStudentQuizSubmissions(studentId);
        List<QuizSubmissionDTO> submissionDTOs = submissions.stream()
                .map(this::toQuizSubmissionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(submissionDTOs);
    }

    @GetMapping("/quizzes/{quizId}")
    public ResponseEntity<QuizDTO> getQuiz(@PathVariable Long quizId) {
        Quiz quiz = quizService.getQuizById(quizId);
        return ResponseEntity.ok(toQuizDTO(quiz));
    }

    @GetMapping("/quizzes/{quizId}/questions")
    public ResponseEntity<List<QuestionDTO>> getQuizQuestions(@PathVariable Long quizId) {
        List<Question> questions = quizService.getQuizQuestions(quizId);
        List<QuestionDTO> questionDTOs = questions.stream()
                .map(this::toQuestionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(questionDTOs);
    }

    private QuizDTO toQuizDTO(Quiz quiz) {
        return QuizDTO.builder()
                .id(quiz.getId())
                .title(quiz.getTitle())
                .timeLimit(quiz.getTimeLimit())
                .passingScore(quiz.getPassingScore())
                .moduleTitle(quiz.getModule() != null ? quiz.getModule().getTitle() : null)
                .build();
    }

    private QuestionDTO toQuestionDTO(Question question) {
        return QuestionDTO.builder()
                .id(question.getId())
                .text(question.getText())
                .type(question.getType().name())
                .points(question.getPoints())
                .build();
    }

    private QuizSubmissionDTO toQuizSubmissionDTO(QuizSubmission submission) {
        return QuizSubmissionDTO.builder()
                .id(submission.getId())
                .quizId(submission.getQuiz().getId())
                .quizTitle(submission.getQuiz().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .score(submission.getScore())
                .passed(submission.getPassed())
                .attemptNumber(submission.getAttemptNumber())
                .takenAt(submission.getTakenAt().toString())
                .build();
    }
}
