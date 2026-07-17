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

        String cleanedJson = JsonCleanUtils.cleanResponse(rawResponse);

        try {
            return objectMapper.readValue(cleanedJson, JobMatchAiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map AI response to JobMatchAiResponse. Raw response was: " + rawResponse, e);
        }
    }
}
