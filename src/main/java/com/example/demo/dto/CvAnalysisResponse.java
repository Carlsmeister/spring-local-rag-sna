package com.example.demo.dto;

import java.util.List;

public record CvAnalysisResponse(
        int atsScore,
        int grammarScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> suggestions
) {}