package com.example.demo.document.parser;

import org.apache.tika.Tika;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TikaParserServiceTest {

    private TikaParserService tikaParserService;

    @BeforeEach
    void setUp() {
        tikaParserService = new TikaParserService(new Tika());
    }

    @Test
    void testParseNullInputStream() {
        String result = tikaParserService.parse(null);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseEmptyInputStream() {
        InputStream emptyStream = new ByteArrayInputStream(new byte[0]);
        String result = tikaParserService.parse(emptyStream);
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testParseRealPdfFixture() throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/fixtures/cv_sample.pdf")) {
            assertNotNull(inputStream, "Test fixture cv_sample.pdf not found in classpath!");
            
            String result = tikaParserService.parse(inputStream);
            
            assertNotNull(result);
            assertFalse(result.trim().isEmpty(), "Extracted text should not be empty");
            
            // Check for common CV headings/keywords or applicant's name
            String lowerResult = result.toLowerCase();
            boolean hasExpectedKeyword = lowerResult.contains("carl") 
                    || lowerResult.contains("lundholm") 
                    || lowerResult.contains("experience") 
                    || lowerResult.contains("education") 
                    || lowerResult.contains("cv")
                    || lowerResult.contains("curriculum");
            
            assertTrue(hasExpectedKeyword, "Extracted text should contain CV-related keywords or applicant name");
        }
    }
}
