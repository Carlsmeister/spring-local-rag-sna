package com.example.demo.mapper;

import com.example.demo.job.dto.JobMatchAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class JobMatchMapper {

    private final ObjectMapper objectMapper;

    public JobMatchMapper() {
        this.objectMapper = new ObjectMapper();
    }

    public JobMatchMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Converts a raw AI response string (potentially containing markdown and conversational fluff)
     * into a {@link JobMatchAiResponse} DTO.
     *
     * @param rawResponse the raw response from the AI model
     * @return the parsed JobMatchAiResponse
     * @throws IllegalArgumentException if the raw response is null or blank
     * @throws RuntimeException if the response cannot be parsed into the expected DTO structure
     */
    public JobMatchAiResponse toJobMatchAiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalArgumentException("AI response cannot be null or empty");
        }

        String cleanedJson = cleanResponse(rawResponse);

        try {
            return objectMapper.readValue(cleanedJson, JobMatchAiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map AI response to JobMatchAiResponse. Raw response was: " + rawResponse, e);
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
