package com.example.demo.cv.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record CvAnalysisResponseDto(
    UUID analysisId,
    UUID documentId,
    int atsScore,
    List<String> strengths,
    List<String> weaknesses,
    List<String> recommendations,
    LocalDateTime analyzedAt
) {}
