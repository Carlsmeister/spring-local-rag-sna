package com.example.demo.mapper;

import com.example.demo.job.dto.JobMatchAiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JobMatchMapperTest {

    private JobMatchMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new JobMatchMapper();
    }

    @Test
    void testToJobMatchAiResponse_CleanJson() {
        String json = """
            {
                "matchPercentage": 85,
                "missingKeywords": ["Docker", "Kubernetes"],
                "matchingStrength": "Strong backend experience.",
                "actionableSuggestions": ["Add AWS details"]
            }
            """;

        JobMatchAiResponse response = mapper.toJobMatchAiResponse(json);

        assertNotNull(response);
        assertEquals(85, response.matchPercentage());
        assertEquals(2, response.missingKeywords().size());
        assertTrue(response.missingKeywords().contains("Docker"));
        assertEquals("Strong backend experience.", response.matchingStrength());
        assertEquals(1, response.actionableSuggestions().size());
        assertEquals("Add AWS details", response.actionableSuggestions().get(0));
    }

    @Test
    void testToJobMatchAiResponse_MarkdownJson() {
        String json = """
            ```json
            {
                "matchPercentage": 72,
                "missingKeywords": ["Spring Cloud"],
                "matchingStrength": "Good Java skills.",
                "actionableSuggestions": []
            }
            ```
            """;

        JobMatchAiResponse response = mapper.toJobMatchAiResponse(json);

        assertNotNull(response);
        assertEquals(72, response.matchPercentage());
        assertEquals(1, response.missingKeywords().size());
        assertEquals("Spring Cloud", response.missingKeywords().get(0));
        assertEquals("Good Java skills.", response.matchingStrength());
        assertTrue(response.actionableSuggestions().isEmpty());
    }

    @Test
    void testToJobMatchAiResponse_ConversationalFluff() {
        String json = """
            Sure, here is the analysis:
            
            ```
            {
                "matchPercentage": 90,
                "missingKeywords": [],
                "matchingStrength": "Perfect match.",
                "actionableSuggestions": ["No suggestions."]
            }
            ```
            
            I hope this helps you with your job search!
            """;

        JobMatchAiResponse response = mapper.toJobMatchAiResponse(json);

        assertNotNull(response);
        assertEquals(90, response.matchPercentage());
        assertTrue(response.missingKeywords().isEmpty());
        assertEquals("Perfect match.", response.matchingStrength());
        assertEquals(1, response.actionableSuggestions().size());
    }

    @Test
    void testToJobMatchAiResponse_NullOrBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> mapper.toJobMatchAiResponse(null));
        assertThrows(IllegalArgumentException.class, () -> mapper.toJobMatchAiResponse("   "));
    }

    @Test
    void testToJobMatchAiResponse_MalformedJsonThrowsException() {
        String malformed = """
            {
                "matchPercentage": "not-an-integer",
                "missingKeywords": [
            }
            """;
        assertThrows(RuntimeException.class, () -> mapper.toJobMatchAiResponse(malformed));
    }
}
