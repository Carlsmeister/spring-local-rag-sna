package com.example.demo.job.dto;

import java.util.List;

public record JobMatchResponseDto(
    Integer matchPercentage,
    List<String> missingKeywords,
    String matchingStrength,
    List<String> actionableSuggestions
) {}
