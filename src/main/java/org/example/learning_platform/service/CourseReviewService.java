package org.example.learning_platform.service;
import lombok.RequiredArgsConstructor;
import org.example.learning_platform.model.Course;
import org.example.learning_platform.model.CourseReview;
import org.example.learning_platform.model.User;
import org.example.learning_platform.repository.CourseRepository;
import org.example.learning_platform.repository.CourseReviewRepository;
import org.example.learning_platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CourseReviewService {
    private final CourseReviewRepository reviewRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    @Transactional
    public CourseReview createReview(Long courseId, Long studentId, Integer rating, String comment) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        User student = userRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        if (student.getRole() != User.UserRole.STUDENT) {
            throw new IllegalArgumentException("User is not a student");
        }
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
        CourseReview review = CourseReview.builder()
                .course(course)
                .student(student)
                .rating(rating)
                .comment(comment)
                .createdAt(LocalDateTime.now())
                .build();
        return reviewRepository.save(review);
    }
    @Transactional(readOnly = true)
    public List<CourseReview> getCourseReviews(Long courseId) {
        return reviewRepository.findByCourseId(courseId);
    }
    @Transactional(readOnly = true)
    public Double getCourseAverageRating(Long courseId) {
        return reviewRepository.getAverageRatingByCourseId(courseId);
    }
    @Transactional
    public void deleteReview(Long reviewId) {
        reviewRepository.deleteById(reviewId);
    }
}
