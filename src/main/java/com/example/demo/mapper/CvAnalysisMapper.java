package com.example.demo.mapper;

import com.example.demo.dto.CvAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class CvAnalysisMapper {

    private final ObjectMapper objectMapper;

    public CvAnalysisMapper() {
        this.objectMapper = new ObjectMapper();
    }

    public CvAnalysisMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Converts a raw AI response string (potentially containing markdown and conversational fluff)
     * into a {@link CvAnalysisResponse} DTO.
     *
     * @param rawResponse the raw response from the AI model
     * @return the parsed CvAnalysisResponse
     * @throws IllegalArgumentException if the raw response is null or blank
     * @throws RuntimeException if the response cannot be parsed into the expected DTO structure
     */
    public CvAnalysisResponse toCvAnalysisResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalArgumentException("AI response cannot be null or empty");
        }

        String cleanedJson = cleanResponse(rawResponse);

        try {
            return objectMapper.readValue(cleanedJson, CvAnalysisResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map AI response to CvAnalysisResponse. Raw response was: " + rawResponse, e);
        }
    }

    private String cleanResponse(String rawResponse) {
        String cleaned = rawResponse.trim();

        // 1. Remove markdown block syntax if present
        if (cleaned.startsWith("```")) {
            // Remove opening ```json or ```
            cleaned = cleaned.replaceFirst("^```(?:json)?\\s*", "");
            // Remove closing ```
            cleaned = cleaned.replaceFirst("\\s*```$", "");
        }

        cleaned = cleaned.trim();

        // 2. Extract valid JSON object if there's any surrounding fluff
        int firstBrace = cleaned.indexOf('{');
        int lastBrace = cleaned.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            cleaned = cleaned.substring(firstBrace, lastBrace + 1);
        }

        return cleaned;
    }
}
