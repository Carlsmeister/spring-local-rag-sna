package com.example.demo.mapper;

/**
 * Utility class for cleaning raw AI responses before mapping them to JSON objects.
 */
public final class JsonCleanUtils {

    private JsonCleanUtils() {
        // Prevent instantiation
    }

    /**
     * Cleans raw AI response strings by removing markdown code block ticks and extracting
     * the valid JSON object structure.
     *
     * @param rawResponse the raw response from the AI model
     * @return the cleaned JSON string, or null if the input is null
     */
    public static String cleanResponse(String rawResponse) {
        if (rawResponse == null) {
            return null;
        }
        
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
