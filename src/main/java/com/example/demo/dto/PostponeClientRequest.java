package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostponeClientRequest {
    @NotNull(message = "Client ID is required")
    private Long clientId;

    @NotNull(message = "Queue ID is required")
    private Long queueId;

    @NotNull(message = "Postpone minutes is required")
    @Min(value = 1, message = "Postpone minutes must be at least 1")
    private Integer postponeMinutes;
}
