package com.example.demo.job.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record JobMatchRequest(
    @NotNull(message = "Document ID is required")
    UUID documentId,

    @NotBlank(message = "Job advertisement description text is required")
    String jobAdText
) {}
