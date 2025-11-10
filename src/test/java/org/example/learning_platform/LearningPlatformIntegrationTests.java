package org.example.learning_platform;

import org.example.learning_platform.model.*;
import org.example.learning_platform.repository.*;
import org.example.learning_platform.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LearningPlatformIntegrationTests {

    @Autowired private UserService userService;
    @Autowired private CategoryService categoryService;
    @Autowired private CourseService courseService;
    @Autowired private EnrollmentService enrollmentService;
    @Autowired private CourseContentService contentService;
    @Autowired private SubmissionService submissionService;
    @Autowired private QuizService quizService;
    @Autowired private CourseReviewService reviewService;
    @Autowired private UserRepository userRepository;
    @Autowired private CourseRepository courseRepository;
    @Autowired private ProfileRepository profileRepository;

    @Test
    void contextLoads() {
        assertThat(userService).isNotNull();
        assertThat(courseService).isNotNull();
    }

    @Test
    @Transactional
    void testCreateUserWithProfile() {
        User user = User.builder()
                .name("John Doe").email("john@example.com")
                .role(User.UserRole.STUDENT).phoneNumber("+1234567890").build();
        Profile profile = Profile.builder()
                .bio("Software Developer").avatarUrl("https://example.com/avatar.jpg")
                .linkedinUrl("https://linkedin.com/in/johndoe").build();

        User savedUser = userService.createUserWithProfile(user, profile);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");

        Profile savedProfile = profileRepository.findByUserId(savedUser.getId()).orElseThrow();
        assertThat(savedProfile.getBio()).isEqualTo("Software Developer");
        assertThat(savedProfile.getUser().getName()).isEqualTo("John Doe");
    }

    @Test
    void testLazyInitializationException() {
        Category category = categoryService.createCategory(Category.builder().name("TestCategory").build());
        User teacher = userService.createUser(
                User.builder().name("Teacher").email("lazy@test.com").role(User.UserRole.TEACHER).build());
        Course course = courseService.createCourse(
                Course.builder().title("Test Course").build(), category.getId(), teacher.getId());

        contentService.createModule(course.getId(),
                org.example.learning_platform.model.Module.builder().title("Module 1").orderIndex(1).build());

        Course fetchedCourse = courseRepository.findById(course.getId()).orElseThrow();

        assertThatThrownBy(() -> {
            fetchedCourse.getModules().size();
        }).isInstanceOf(org.hibernate.LazyInitializationException.class);
    }

    @Test
    @Transactional
    void testCreateCourseWithFullStructure() {
        Category category = categoryService.createCategory(
                Category.builder().name("Programming").description("Programming courses").build());
        User teacher = userService.createUser(
                User.builder().name("Dr. Smith").email("smith@example.com").role(User.UserRole.TEACHER).build());
        Course course = courseService.createCourse(
                Course.builder().title("Introduction to Java").description("Learn Java from scratch")
                        .duration("8 weeks").startDate(LocalDate.now()).build(),
                category.getId(), teacher.getId());

        assertThat(course.getId()).isNotNull();
        assertThat(course.getTitle()).isEqualTo("Introduction to Java");
        assertThat(course.getCategory().getName()).isEqualTo("Programming");
        assertThat(course.getTeacher().getName()).isEqualTo("Dr. Smith");
    }

    @Test
    @Transactional
    void testEnrollStudentInCourse() {
        Category category = categoryService.createCategory(Category.builder().name("Math").build());
        User teacher = userService.createUser(
                User.builder().name("Prof. Johnson").email("johnson@example.com").role(User.UserRole.TEACHER).build());
        Course course = courseService.createCourse(
                Course.builder().title("Calculus 101").description("Basic calculus").build(),
                category.getId(), teacher.getId());
        User student = userService.createUser(
                User.builder().name("Alice Student").email("alice@example.com").role(User.UserRole.STUDENT).build());

        Enrollment enrollment = enrollmentService.enrollStudent(student.getId(), course.getId());

        assertThat(enrollment.getId()).isNotNull();
        assertThat(enrollment.getStatus()).isEqualTo(Enrollment.EnrollmentStatus.ACTIVE);
        assertThat(enrollment.getStudent().getName()).isEqualTo("Alice Student");
        assertThat(enrollment.getCourse().getTitle()).isEqualTo("Calculus 101");
    }

    @Test
    @Transactional
    void testCannotEnrollNonStudentInCourse() {
        Category category = categoryService.createCategory(Category.builder().name("Science").build());
        User teacher = userService.createUser(
                User.builder().name("Prof. Brown").email("brown@example.com").role(User.UserRole.TEACHER).build());
        Course course = courseService.createCourse(
                Course.builder().title("Physics").build(), category.getId(), teacher.getId());
        User admin = userService.createUser(
                User.builder().name("Admin User").email("admin@example.com").role(User.UserRole.ADMIN).build());

        assertThatThrownBy(() -> enrollmentService.enrollStudent(admin.getId(), course.getId()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("not a student");
    }

    @Test
    @Transactional
    void testCreateModuleWithLessonsAndAssignments() {
        Category category = categoryService.createCategory(Category.builder().name("CS").build());
        User teacher = userService.createUser(
                User.builder().name("Prof. Davis").email("davis@example.com").role(User.UserRole.TEACHER).build());
        Course course = courseService.createCourse(
                Course.builder().title("Data Structures").build(), category.getId(), teacher.getId());

        org.example.learning_platform.model.Module module = contentService.createModule(course.getId(),
                org.example.learning_platform.model.Module.builder()
                        .title("Arrays and Lists").orderIndex(1).description("Introduction to arrays").build());

        Lesson lesson = contentService.createLesson(module.getId(),
                Lesson.builder().title("Array Basics")
                        .content("Arrays are fundamental data structures...").orderIndex(1).build());

        Assignment assignment = contentService.createAssignment(lesson.getId(),
                Assignment.builder().title("Array Practice").description("Implement array operations")
                        .maxScore(100).dueDate(LocalDateTime.now().plusDays(7)).build());

        assertThat(module.getId()).isNotNull();
        assertThat(module.getCourse().getId()).isEqualTo(course.getId());
        assertThat(lesson.getId()).isNotNull();
        assertThat(lesson.getModule().getId()).isEqualTo(module.getId());
        assertThat(assignment.getId()).isNotNull();
        assertThat(assignment.getLesson().getId()).isEqualTo(lesson.getId());
    }

    @Test
    @Transactional
    void testSubmitAndGradeAssignment() {
        Category category = categoryService.createCategory(Category.builder().name("Programming").build());
        User teacher = userService.createUser(
                User.builder().name("Teacher").email("teacher@test.com").role(User.UserRole.TEACHER).build());
        User student = userService.createUser(
                User.builder().name("Student").email("student@test.com").role(User.UserRole.STUDENT).build());
        Course course = courseService.createCourse(
                Course.builder().title("Java Course").build(), category.getId(), teacher.getId());
        org.example.learning_platform.model.Module module = contentService.createModule(course.getId(),
                org.example.learning_platform.model.Module.builder().title("Module 1").orderIndex(1).build());
        Lesson lesson = contentService.createLesson(module.getId(),
                Lesson.builder().title("Lesson 1").orderIndex(1).build());
        Assignment assignment = contentService.createAssignment(lesson.getId(),
                Assignment.builder().title("Homework 1").description("Complete exercises").maxScore(100).build());

        Submission submission = submissionService.submitAssignment(
                assignment.getId(), student.getId(), "Here is my solution...");
        assertThat(submission.getId()).isNotNull();
        assertThat(submission.getStatus()).isEqualTo(Submission.SubmissionStatus.SUBMITTED);

        Submission gradedSubmission = submissionService.gradeSubmission(submission.getId(), 85, "Good work!");
        assertThat(gradedSubmission.getScore()).isEqualTo(85);
        assertThat(gradedSubmission.getFeedback()).isEqualTo("Good work!");
        assertThat(gradedSubmission.getStatus()).isEqualTo(Submission.SubmissionStatus.GRADED);
    }

    @Test
    @Transactional
    void testCreateQuizWithQuestionsAndSubmit() {
        Category category = categoryService.createCategory(Category.builder().name("Math").build());
        User teacher = userService.createUser(
                User.builder().name("Teacher").email("teacher2@test.com").role(User.UserRole.TEACHER).build());
        User student = userService.createUser(
                User.builder().name("Student").email("student2@test.com").role(User.UserRole.STUDENT).build());
        Course course = courseService.createCourse(
                Course.builder().title("Algebra").build(), category.getId(), teacher.getId());
        org.example.learning_platform.model.Module module = contentService.createModule(course.getId(),
                org.example.learning_platform.model.Module.builder().title("Basics").orderIndex(1).build());

        Quiz quiz = quizService.createQuiz(module.getId(),
                Quiz.builder().title("Module 1 Quiz").timeLimit(30).passingScore(70).build());
        Question question = quizService.addQuestion(quiz.getId(),
                Question.builder().text("What is 2 + 2?").type(Question.QuestionType.SINGLE_CHOICE).points(10).build());
        quizService.addAnswerOption(question.getId(), AnswerOption.builder().text("3").isCorrect(false).build());
        quizService.addAnswerOption(question.getId(), AnswerOption.builder().text("4").isCorrect(true).build());

        QuizSubmission quizSubmission = quizService.submitQuiz(quiz.getId(), student.getId(), 80);

        assertThat(quizSubmission.getId()).isNotNull();
        assertThat(quizSubmission.getScore()).isEqualTo(80);
        assertThat(quizSubmission.getPassed()).isTrue();
        assertThat(quizSubmission.getAttemptNumber()).isEqualTo(1);
    }

    @Test
    @Transactional
    void testCourseReview() {
        Category category = categoryService.createCategory(Category.builder().name("Design").build());
        User teacher = userService.createUser(
                User.builder().name("Teacher").email("teacher3@test.com").role(User.UserRole.TEACHER).build());
        User student = userService.createUser(
                User.builder().name("Student").email("student3@test.com").role(User.UserRole.STUDENT).build());
        Course course = courseService.createCourse(
                Course.builder().title("UI/UX Design").build(), category.getId(), teacher.getId());

        CourseReview review = reviewService.createReview(course.getId(), student.getId(), 5, "Excellent course!");

        assertThat(review.getId()).isNotNull();
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getComment()).isEqualTo("Excellent course!");

        Double avgRating = reviewService.getCourseAverageRating(course.getId());
        assertThat(avgRating).isEqualTo(5.0);
    }

    @Test
    @Transactional
    void testCourseTags() {
        Category category = categoryService.createCategory(Category.builder().name("Tech").build());
        User teacher = userService.createUser(
                User.builder().name("Teacher").email("teacher4@test.com").role(User.UserRole.TEACHER).build());
        Course course = courseService.createCourse(
                Course.builder().title("Spring Boot").build(), category.getId(), teacher.getId());

        courseService.addTagsToCourse(course.getId(), java.util.Set.of("Java", "Spring", "Backend"));

        Course updatedCourse = courseService.getCourseById(course.getId());
        assertThat(updatedCourse.getTags()).hasSize(3);
        assertThat(updatedCourse.getTags()).extracting("name")
                .containsExactlyInAnyOrder("Java", "Spring", "Backend");

        List<Course> javaCourses = courseService.getCoursesByTag("Java");
        assertThat(javaCourses).hasSize(1);
        assertThat(javaCourses.get(0).getTitle()).isEqualTo("Spring Boot");
    }

    @Test
    @Transactional
    void testCascadeDelete() {
        Category category = categoryService.createCategory(Category.builder().name("Test").build());
        User teacher = userService.createUser(
                User.builder().name("Teacher").email("teacher5@test.com").role(User.UserRole.TEACHER).build());
        Course course = courseService.createCourse(
                Course.builder().title("Test Course").build(), category.getId(), teacher.getId());
        org.example.learning_platform.model.Module module = contentService.createModule(course.getId(),
                org.example.learning_platform.model.Module.builder().title("Module").orderIndex(1).build());
        Lesson lesson = contentService.createLesson(module.getId(),
                Lesson.builder().title("Lesson").orderIndex(1).build());

        Long courseId = course.getId();
        courseService.deleteCourse(courseId);
        assertThat(courseRepository.findById(courseId)).isEmpty();
    }

    @Test
    @Transactional
    void testComplexQueryScenario() {
        Category category = categoryService.createCategory(Category.builder().name("Advanced").build());
        User teacher = userService.createUser(
                User.builder().name("Prof").email("prof@test.com").role(User.UserRole.TEACHER).build());
        User student1 = userService.createUser(
                User.builder().name("Student1").email("s1@test.com").role(User.UserRole.STUDENT).build());
        User student2 = userService.createUser(
                User.builder().name("Student2").email("s2@test.com").role(User.UserRole.STUDENT).build());
        Course course = courseService.createCourse(
                Course.builder().title("Advanced Java").build(), category.getId(), teacher.getId());

        enrollmentService.enrollStudent(student1.getId(), course.getId());
        enrollmentService.enrollStudent(student2.getId(), course.getId());

        List<Enrollment> courseEnrollments = enrollmentService.getCourseEnrollments(course.getId());
        List<Enrollment> student1Enrollments = enrollmentService.getStudentEnrollments(student1.getId());

        assertThat(courseEnrollments).hasSize(2);
        assertThat(student1Enrollments).hasSize(1);
        assertThat(student1Enrollments.get(0).getCourse().getTitle()).isEqualTo("Advanced Java");
    }

    @Test
    @Transactional
    void testUniqueConstraints() {
        userService.createUser(
                User.builder().name("Student").email("unique@test.com").role(User.UserRole.STUDENT).build());

        assertThatThrownBy(() -> userService.createUser(
                User.builder().name("Another").email("unique@test.com").role(User.UserRole.ADMIN).build()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("already exists");
    }

    @Test
    @Transactional
    void testGetUsersByRole() {
        userService.createUser(User.builder().name("T1").email("t1@test.com").role(User.UserRole.TEACHER).build());
        userService.createUser(User.builder().name("T2").email("t2@test.com").role(User.UserRole.TEACHER).build());
        userService.createUser(User.builder().name("S1").email("s1test@test.com").role(User.UserRole.STUDENT).build());

        List<User> teachers = userService.getUsersByRole(User.UserRole.TEACHER);
        List<User> students = userService.getUsersByRole(User.UserRole.STUDENT);

        assertThat(teachers).hasSize(2);
        assertThat(students).hasSize(1);
    }
}

