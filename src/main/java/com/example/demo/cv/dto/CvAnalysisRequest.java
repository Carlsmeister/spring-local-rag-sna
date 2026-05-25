package com.example.demo.cv.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CvAnalysisRequest(
    @NotNull(message = "documentId must not be null")
    UUID documentId
) {}
