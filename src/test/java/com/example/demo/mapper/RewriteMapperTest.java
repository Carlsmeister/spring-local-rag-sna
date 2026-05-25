package com.example.demo.mapper;

import com.example.demo.rewrite.dto.RewriteAiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RewriteMapperTest {

    private RewriteMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RewriteMapper();
    }

    @Test
    void testToRewriteAiResponse_CleanJson() {
        String json = """
            {
                "originalText": "Developed new features.",
                "suggestions": [
                    {
                        "style": "ACTION_ORIENTED",
                        "text": "Engineered robust new features."
                    },
                    {
                        "style": "CONCISE",
                        "text": "Created features."
                    }
                ]
            }
            """;

        RewriteAiResponse response = mapper.toRewriteAiResponse(json);

        assertNotNull(response);
        assertEquals("Developed new features.", response.originalText());
        assertEquals(2, response.suggestions().size());
        assertEquals("ACTION_ORIENTED", response.suggestions().get(0).style());
        assertEquals("Engineered robust new features.", response.suggestions().get(0).text());
        assertEquals("CONCISE", response.suggestions().get(1).style());
        assertEquals("Created features.", response.suggestions().get(1).text());
    }

    @Test
    void testToRewriteAiResponse_MarkdownJson() {
        String json = """
            ```json
            {
                "originalText": "Managed team.",
                "suggestions": [
                    {
                        "style": "METRIC_DRIVEN",
                        "text": "Led 5 engineers."
                    }
                ]
            }
            ```
            """;

        RewriteAiResponse response = mapper.toRewriteAiResponse(json);

        assertNotNull(response);
        assertEquals("Managed team.", response.originalText());
        assertEquals(1, response.suggestions().size());
        assertEquals("METRIC_DRIVEN", response.suggestions().get(0).style());
        assertEquals("Led 5 engineers.", response.suggestions().get(0).text());
    }

    @Test
    void testToRewriteAiResponse_ConversationalFluff() {
        String json = """
            Sure! Here are your section rewrite variations:
            
            ```
            {
                "originalText": "Speeds optimization.",
                "suggestions": [
                    {
                        "style": "CONCISE",
                        "text": "Optimized speed."
                    }
                ]
            }
            ```
            
            Let me know if you need anything else!
            """;

        RewriteAiResponse response = mapper.toRewriteAiResponse(json);

        assertNotNull(response);
        assertEquals("Speeds optimization.", response.originalText());
        assertEquals(1, response.suggestions().size());
        assertEquals("CONCISE", response.suggestions().get(0).style());
        assertEquals("Optimized speed.", response.suggestions().get(0).text());
    }

    @Test
    void testToRewriteAiResponse_NullOrBlankThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> mapper.toRewriteAiResponse(null));
        assertThrows(IllegalArgumentException.class, () -> mapper.toRewriteAiResponse("   "));
    }

    @Test
    void testToRewriteAiResponse_MalformedJsonThrowsException() {
        String malformed = """
            {
                "originalText": "Failing",
                "suggestions": [
            }
            """;
        assertThrows(RuntimeException.class, () -> mapper.toRewriteAiResponse(malformed));
    }
}
