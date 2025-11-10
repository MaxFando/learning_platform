package org.example.learning_platform.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizTakeRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Score is required")
    private Integer score;
}

