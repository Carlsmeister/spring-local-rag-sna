package com.example.demo.dto;

import java.util.List;

public record CvAnalysisResponse(
        int atsScore,
        List<String> strengths,
        List<String> weaknesses,
        List<String> recommendations
) {}