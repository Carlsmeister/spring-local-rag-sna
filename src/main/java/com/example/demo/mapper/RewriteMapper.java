package com.example.demo.mapper;

import com.example.demo.rewrite.dto.RewriteAiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

@Component
public class RewriteMapper {

    private final ObjectMapper objectMapper;

    public RewriteMapper() {
        this.objectMapper = new ObjectMapper();
    }

    public RewriteMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }

    /**
     * Converts a raw AI response string (potentially containing markdown and conversational fluff)
     * into a {@link RewriteAiResponse} DTO.
     *
     * @param rawResponse the raw response from the AI model
     * @return the parsed RewriteAiResponse
     * @throws IllegalArgumentException if the raw response is null or blank
     * @throws RuntimeException if the response cannot be parsed into the expected DTO structure
     */
    public RewriteAiResponse toRewriteAiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            throw new IllegalArgumentException("AI response cannot be null or empty");
        }

        String cleanedJson = JsonCleanUtils.cleanResponse(rawResponse);

        try {
            return objectMapper.readValue(cleanedJson, RewriteAiResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to map AI response to RewriteAiResponse. Raw response was: " + rawResponse, e);
        }
    }
}
