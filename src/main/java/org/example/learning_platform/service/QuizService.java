package org.example.learning_platform.service;

import lombok.RequiredArgsConstructor;
import org.example.learning_platform.model.*;
import org.example.learning_platform.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuizService {
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final AnswerOptionRepository answerOptionRepository;
    private final QuizSubmissionRepository quizSubmissionRepository;
    private final ModuleRepository moduleRepository;
    private final UserRepository userRepository;

    @Transactional
    public Quiz createQuiz(Long moduleId, Quiz quiz) {
        org.example.learning_platform.model.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        quiz.setModule(module);
        return quizRepository.save(quiz);
    }

    @Transactional
    public Question addQuestion(Long quizId, Question question) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        question.setQuiz(quiz);
        return questionRepository.save(question);
    }

    @Transactional
    public AnswerOption addAnswerOption(Long questionId, AnswerOption option) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        option.setQuestion(question);
        return answerOptionRepository.save(option);
    }

    @Transactional
    public QuizSubmission submitQuiz(Long quizId, Long studentId, Integer score) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (student.getRole() != User.UserRole.STUDENT) {
            throw new IllegalArgumentException("User is not a student");
        }
        List<QuizSubmission> previousAttempts = quizSubmissionRepository.findByQuizIdAndStudentId(quizId, studentId);
        int attemptNumber = previousAttempts.size() + 1;
        boolean passed = quiz.getPassingScore() != null && score >= quiz.getPassingScore();
        QuizSubmission submission = QuizSubmission.builder()
                .quiz(quiz)
                .student(student)
                .score(score)
                .takenAt(LocalDateTime.now())
                .passed(passed)
                .attemptNumber(attemptNumber)
                .build();
        return quizSubmissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public Quiz getQuizById(Long id) {
        return quizRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
    }

    @Transactional(readOnly = true)
    public List<Question> getQuizQuestions(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    @Transactional(readOnly = true)
    public List<AnswerOption> getQuestionOptions(Long questionId) {
        return answerOptionRepository.findByQuestionId(questionId);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> getQuizSubmissions(Long quizId) {
        return quizSubmissionRepository.findByQuizId(quizId);
    }

    @Transactional(readOnly = true)
    public List<QuizSubmission> getStudentQuizSubmissions(Long studentId) {
        return quizSubmissionRepository.findByStudentId(studentId);
    }
}
