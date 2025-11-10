package org.example.learning_platform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseDTO {
    private Long id;
    private String title;
    private String description;
    private String duration;
    private LocalDate startDate;
    private String categoryName;
    private String teacherName;
    private Set<String> tags;
    private Double averageRating;
}
