package org.example.learning_platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learning_platform.dto.*;
import org.example.learning_platform.model.Assignment;
import org.example.learning_platform.model.Submission;
import org.example.learning_platform.service.CourseContentService;
import org.example.learning_platform.service.SubmissionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AssignmentController {

    private final CourseContentService contentService;
    private final SubmissionService submissionService;

    @PostMapping("/lessons/{lessonId}/assignments")
    public ResponseEntity<AssignmentDTO> createAssignment(
            @PathVariable Long lessonId,
            @Valid @RequestBody AssignmentCreateRequest request) {

        Assignment assignment = Assignment.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .maxScore(request.getMaxScore())
                .build();

        Assignment createdAssignment = contentService.createAssignment(lessonId, assignment);
        return ResponseEntity.status(HttpStatus.CREATED).body(toAssignmentDTO(createdAssignment));
    }

    @PostMapping("/assignments/{assignmentId}/submit")
    public ResponseEntity<SubmissionDTO> submitAssignment(
            @PathVariable Long assignmentId,
            @Valid @RequestBody SubmissionCreateRequest request) {

        Submission submission = submissionService.submitAssignment(
                assignmentId,
                request.getStudentId(),
                request.getContent()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toSubmissionDTO(submission));
    }

    @PutMapping("/submissions/{submissionId}/grade")
    public ResponseEntity<SubmissionDTO> gradeSubmission(
            @PathVariable Long submissionId,
            @Valid @RequestBody GradeSubmissionRequest request) {

        Submission gradedSubmission = submissionService.gradeSubmission(
                submissionId,
                request.getScore(),
                request.getFeedback()
        );

        return ResponseEntity.ok(toSubmissionDTO(gradedSubmission));
    }

    @GetMapping("/students/{studentId}/submissions")
    public ResponseEntity<List<SubmissionDTO>> getStudentSubmissions(@PathVariable Long studentId) {
        List<Submission> submissions = submissionService.getStudentSubmissions(studentId);
        List<SubmissionDTO> submissionDTOs = submissions.stream()
                .map(this::toSubmissionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(submissionDTOs);
    }

    @GetMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<List<SubmissionDTO>> getAssignmentSubmissions(@PathVariable Long assignmentId) {
        List<Submission> submissions = submissionService.getAssignmentSubmissions(assignmentId);
        List<SubmissionDTO> submissionDTOs = submissions.stream()
                .map(this::toSubmissionDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(submissionDTOs);
    }

    private AssignmentDTO toAssignmentDTO(Assignment assignment) {
        return AssignmentDTO.builder()
                .id(assignment.getId())
                .title(assignment.getTitle())
                .description(assignment.getDescription())
                .dueDate(assignment.getDueDate() != null ? assignment.getDueDate().toString() : null)
                .maxScore(assignment.getMaxScore())
                .lessonTitle(assignment.getLesson() != null ? assignment.getLesson().getTitle() : null)
                .build();
    }

    private SubmissionDTO toSubmissionDTO(Submission submission) {
        return SubmissionDTO.builder()
                .id(submission.getId())
                .assignmentId(submission.getAssignment().getId())
                .assignmentTitle(submission.getAssignment().getTitle())
                .studentId(submission.getStudent().getId())
                .studentName(submission.getStudent().getName())
                .content(submission.getContent())
                .score(submission.getScore())
                .feedback(submission.getFeedback())
                .status(submission.getStatus() != null ? submission.getStatus().name() : null)
                .submittedAt(submission.getSubmittedAt().toString())
                .build();
    }
}

