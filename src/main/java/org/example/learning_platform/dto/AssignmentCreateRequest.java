package org.example.learning_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignmentCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private String description;
    
    private LocalDateTime dueDate;
    
    @NotNull(message = "Max score is required")
    private Integer maxScore;
}

