package org.example.learning_platform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizCreateRequest {
    @NotBlank(message = "Title is required")
    private String title;
    
    private Integer timeLimit;
    
    @NotNull(message = "Passing score is required")
    private Integer passingScore;
}

