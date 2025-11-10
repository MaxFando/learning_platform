package org.example.learning_platform.service;

import lombok.RequiredArgsConstructor;
import org.example.learning_platform.model.Assignment;
import org.example.learning_platform.model.Course;
import org.example.learning_platform.model.Lesson;
import org.example.learning_platform.repository.AssignmentRepository;
import org.example.learning_platform.repository.CourseRepository;
import org.example.learning_platform.repository.LessonRepository;
import org.example.learning_platform.repository.ModuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseContentService {
    private final ModuleRepository moduleRepository;
    private final LessonRepository lessonRepository;
    private final AssignmentRepository assignmentRepository;
    private final CourseRepository courseRepository;

    @Transactional
    public org.example.learning_platform.model.Module createModule(Long courseId, org.example.learning_platform.model.Module module) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
        module.setCourse(course);
        return moduleRepository.save(module);
    }

    @Transactional
    public Lesson createLesson(Long moduleId, Lesson lesson) {
        org.example.learning_platform.model.Module module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new IllegalArgumentException("Module not found"));
        lesson.setModule(module);
        return lessonRepository.save(lesson);
    }

    @Transactional
    public Assignment createAssignment(Long lessonId, Assignment assignment) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        assignment.setLesson(lesson);
        return assignmentRepository.save(assignment);
    }

    @Transactional(readOnly = true)
    public List<org.example.learning_platform.model.Module> getCourseModules(Long courseId) {
        return moduleRepository.findByCourseIdOrderByOrderIndex(courseId);
    }

    @Transactional(readOnly = true)
    public List<Lesson> getModuleLessons(Long moduleId) {
        return lessonRepository.findByModuleIdOrderByOrderIndex(moduleId);
    }

    @Transactional(readOnly = true)
    public List<Assignment> getLessonAssignments(Long lessonId) {
        return assignmentRepository.findByLessonId(lessonId);
    }

    @Transactional
    public void deleteModule(Long moduleId) {
        moduleRepository.deleteById(moduleId);
    }

    @Transactional
    public void deleteLesson(Long lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    @Transactional
    public void deleteAssignment(Long assignmentId) {
        assignmentRepository.deleteById(assignmentId);
    }
}
