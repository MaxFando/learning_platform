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
public class QuestionCreateRequest {
    @NotBlank(message = "Question text is required")
    private String text;
    
    @NotBlank(message = "Question type is required")
    private String type; // SINGLE_CHOICE, MULTIPLE_CHOICE, TRUE_FALSE
    
    @NotNull(message = "Points is required")
    private Integer points;
}

