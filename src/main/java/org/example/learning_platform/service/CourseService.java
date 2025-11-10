package org.example.learning_platform.service;

import lombok.RequiredArgsConstructor;
import org.example.learning_platform.model.Category;
import org.example.learning_platform.model.Course;
import org.example.learning_platform.model.Tag;
import org.example.learning_platform.model.User;
import org.example.learning_platform.repository.CategoryRepository;
import org.example.learning_platform.repository.CourseRepository;
import org.example.learning_platform.repository.TagRepository;
import org.example.learning_platform.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    @Transactional
    public Course createCourse(Course course, Long categoryId, Long teacherId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new IllegalArgumentException("Teacher not found"));

        if (teacher.getRole() != User.UserRole.TEACHER && teacher.getRole() != User.UserRole.ADMIN) {
            throw new IllegalArgumentException("User is not a teacher");
        }

        course.setCategory(category);
        course.setTeacher(teacher);
        return courseRepository.save(course);
    }

    @Transactional
    public Course addTagsToCourse(Long courseId, Set<String> tagNames) {
        Course course = getCourseById(courseId);

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().name(tagName).build()));
            course.getTags().add(tag);
        }

        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public Course getCourseById(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByCategory(Long categoryId) {
        return courseRepository.findByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByTeacher(Long teacherId) {
        return courseRepository.findByTeacherId(teacherId);
    }

    @Transactional(readOnly = true)
    public List<Course> getCoursesByTag(String tagName) {
        return courseRepository.findByTagName(tagName);
    }

    @Transactional
    public void deleteCourse(Long id) {
        courseRepository.deleteById(id);
    }
}

