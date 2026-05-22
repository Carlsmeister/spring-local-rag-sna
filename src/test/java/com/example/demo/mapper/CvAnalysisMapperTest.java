package com.example.demo.mapper;

import com.example.demo.dto.CvAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CvAnalysisMapperTest {

    private CvAnalysisMapper cvAnalysisMapper;

    @BeforeEach
    void setUp() {
        // Instantiate the ObjectMapper and the mapper under test in isolation
        ObjectMapper objectMapper = new ObjectMapper();
        cvAnalysisMapper = new CvAnalysisMapper(objectMapper);
    }

    @Test
    void testToCvAnalysisResponse_CleanJson() {
        String cleanJson = """
                {
                  "atsScore": 85,
                  "grammarScore": 90,
                  "strengths": ["Clear layout", "Strong skills section"],
                  "weaknesses": ["Lack of action verbs"],
                  "suggestions": ["Add action verbs to experience section"]
                }
                """;

        CvAnalysisResponse response = cvAnalysisMapper.toCvAnalysisResponse(cleanJson);

        assertNotNull(response);
        assertEquals(85, response.atsScore());
        assertEquals(90, response.grammarScore());
        assertEquals(List.of("Clear layout", "Strong skills section"), response.strengths());
        assertEquals(List.of("Lack of action verbs"), response.weaknesses());
        assertEquals(List.of("Add action verbs to experience section"), response.suggestions());
    }

    @Test
    void testToCvAnalysisResponse_MarkdownWrapped() {
        String markdownJson = """
                ```json
                {
                  "atsScore": 70,
                  "grammarScore": 80,
                  "strengths": ["Experience"],
                  "weaknesses": [],
                  "suggestions": []
                }
                ```
                """;

        CvAnalysisResponse response = cvAnalysisMapper.toCvAnalysisResponse(markdownJson);

        assertNotNull(response);
        assertEquals(70, response.atsScore());
        assertEquals(80, response.grammarScore());
        assertEquals(List.of("Experience"), response.strengths());
        assertTrue(response.weaknesses().isEmpty());
        assertTrue(response.suggestions().isEmpty());
    }

    @Test
    void testToCvAnalysisResponse_ConversationalFluff() {
        String conversationalJson = """
                Here is the analysis of your CV in the requested JSON format:
                
                ```json
                {
                  "atsScore": 95,
                  "grammarScore": 95,
                  "strengths": ["Excellent summary"],
                  "weaknesses": ["Formatting mismatch"],
                  "suggestions": ["Adjust margins"]
                }
                ```
                
                Hope this feedback helps you improve your CV!
                """;

        CvAnalysisResponse response = cvAnalysisMapper.toCvAnalysisResponse(conversationalJson);

        assertNotNull(response);
        assertEquals(95, response.atsScore());
        assertEquals(95, response.grammarScore());
        assertEquals(List.of("Excellent summary"), response.strengths());
        assertEquals(List.of("Formatting mismatch"), response.weaknesses());
        assertEquals(List.of("Adjust margins"), response.suggestions());
    }

    @Test
    void testToCvAnalysisResponse_NullOrBlank_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> cvAnalysisMapper.toCvAnalysisResponse(null));
        assertThrows(IllegalArgumentException.class, () -> cvAnalysisMapper.toCvAnalysisResponse("   "));
    }

    @Test
    void testToCvAnalysisResponse_InvalidJson_ThrowsException() {
        String invalidJson = """
                {
                  "atsScore": "not a number",
                  "grammarScore": 90
                }
                """;

        assertThrows(RuntimeException.class, () -> cvAnalysisMapper.toCvAnalysisResponse(invalidJson));
    }
}
