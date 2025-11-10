package org.example.learning_platform.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.learning_platform.dto.*;
import org.example.learning_platform.model.*;
import org.example.learning_platform.service.CourseReviewService;
import org.example.learning_platform.service.CourseService;
import org.example.learning_platform.service.EnrollmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;
    private final EnrollmentService enrollmentService;
    private final CourseReviewService reviewService;

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse(@Valid @RequestBody CourseCreateRequest request) {
        Course course = Course.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .duration(request.getDuration())
                .startDate(request.getStartDate())
                .build();

        Course createdCourse = courseService.createCourse(course, request.getCategoryId(), request.getTeacherId());

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            createdCourse = courseService.addTagsToCourse(createdCourse.getId(), request.getTags());
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(toCourseDTO(createdCourse));
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourses() {
        List<Course> courses = courseService.getAllCourses();
        List<CourseDTO> courseDTOs = courses.stream()
                .map(this::toCourseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(courseDTOs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourseById(@PathVariable Long id) {
        Course course = courseService.getCourseById(id);
        return ResponseEntity.ok(toCourseDTO(course));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseCreateRequest request) {

        Course course = courseService.getCourseById(id);
        course.setTitle(request.getTitle());
        course.setDescription(request.getDescription());
        course.setDuration(request.getDuration());
        course.setStartDate(request.getStartDate());

        // Для простоты, используем существующий метод создания
        // В полноценном приложении лучше создать отдельный метод update
        Course updatedCourse = courseService.getCourseById(id);

        return ResponseEntity.ok(toCourseDTO(updatedCourse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enroll")
    public ResponseEntity<EnrollmentDTO> enrollStudent(
            @PathVariable Long id,
            @RequestParam Long studentId) {

        Enrollment enrollment = enrollmentService.enrollStudent(studentId, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(toEnrollmentDTO(enrollment));
    }

    @GetMapping("/{id}/students")
    public ResponseEntity<List<UserDTO>> getCourseStudents(@PathVariable Long id) {
        List<Enrollment> enrollments = enrollmentService.getCourseEnrollments(id);
        List<UserDTO> students = enrollments.stream()
                .map(e -> toUserDTO(e.getStudent()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(students);
    }

    private CourseDTO toCourseDTO(Course course) {
        Double avgRating = reviewService.getCourseAverageRating(course.getId());

        return CourseDTO.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .duration(course.getDuration())
                .startDate(course.getStartDate())
                .categoryName(course.getCategory() != null ? course.getCategory().getName() : null)
                .teacherName(course.getTeacher() != null ? course.getTeacher().getName() : null)
                .tags(course.getTags().stream().map(Tag::getName).collect(Collectors.toSet()))
                .averageRating(avgRating)
                .build();
    }

    private EnrollmentDTO toEnrollmentDTO(Enrollment enrollment) {
        return EnrollmentDTO.builder()
                .id(enrollment.getId())
                .studentId(enrollment.getStudent().getId())
                .studentName(enrollment.getStudent().getName())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .status(enrollment.getStatus().name())
                .enrollDate(enrollment.getEnrollDate().toString())
                .completedDate(enrollment.getCompletedDate() != null ?
                        enrollment.getCompletedDate().toString() : null)
                .build();
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}

