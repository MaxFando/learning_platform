package org.example.learning_platform.service;

import lombok.RequiredArgsConstructor;
import org.example.learning_platform.model.Assignment;
import org.example.learning_platform.model.Submission;
import org.example.learning_platform.model.User;
import org.example.learning_platform.repository.AssignmentRepository;
import org.example.learning_platform.repository.SubmissionRepository;
import org.example.learning_platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final AssignmentRepository assignmentRepository;
    private final UserRepository userRepository;

    @Transactional
    public Submission submitAssignment(Long assignmentId, Long studentId, String content) {
        Assignment assignment = assignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new IllegalArgumentException("Assignment not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        if (student.getRole() != User.UserRole.STUDENT) {
            throw new IllegalArgumentException("User is not a student");
        }

        // Check if already submitted
        if (submissionRepository.findByAssignmentIdAndStudentId(assignmentId, studentId).isPresent()) {
            throw new IllegalArgumentException("Assignment already submitted by this student");
        }

        Submission submission = Submission.builder()
                .assignment(assignment)
                .student(student)
                .content(content)
                .submittedAt(LocalDateTime.now())
                .status(Submission.SubmissionStatus.SUBMITTED)
                .build();

        return submissionRepository.save(submission);
    }

    @Transactional
    public Submission gradeSubmission(Long submissionId, Integer score, String feedback) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        submission.setScore(score);
        submission.setFeedback(feedback);
        submission.setStatus(Submission.SubmissionStatus.GRADED);

        return submissionRepository.save(submission);
    }

    @Transactional(readOnly = true)
    public List<Submission> getAssignmentSubmissions(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    @Transactional(readOnly = true)
    public List<Submission> getStudentSubmissions(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    @Transactional(readOnly = true)
    public Submission getSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
    }
}

