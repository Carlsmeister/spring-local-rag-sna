package com.example.demo.ai.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactualIntegrityServiceTest {

    private FactualIntegrityService service;

    @BeforeEach
    void setUp() {
        service = new FactualIntegrityService();
    }

    @Test
    void testValidate_Success_NoFabrications() {
        String original = "Responsible for building web applications and managing databases.";
        String suggestion = "Engineered robust and scalable web applications while handling high performance database connections.";
        List<String> keywords = List.of("scalable", "database");

        assertDoesNotThrow(() -> service.validateFactualIntegrity(original, keywords, suggestion));
    }

    @Test
    void testValidate_Success_IncludesAllowedKeywords() {
        String original = "Developed services in Java.";
        String suggestion = "Engineered clean Java applications using Spring Boot framework.";
        List<String> keywords = List.of("Spring", "Boot");

        assertDoesNotThrow(() -> service.validateFactualIntegrity(original, keywords, suggestion));
    }

    @Test
    void testValidate_Failure_FabricatedMetric() {
        String original = "Led a team of developers.";
        String suggestion = "Led a high performing team of 5 developers.";
        List<String> keywords = List.of("team");

        FactualValidationException ex = assertThrows(FactualValidationException.class,
                () -> service.validateFactualIntegrity(original, keywords, suggestion));
        assertTrue(ex.getMessage().contains("Fabricated numeric metric '5'"));
    }

    @Test
    void testValidate_Failure_FabricatedProperNoun() {
        String original = "Developed cloud applications.";
        String suggestion = "Engineered secure cloud applications in AWS.";
        List<String> keywords = List.of("cloud");

        FactualValidationException ex = assertThrows(FactualValidationException.class,
                () -> service.validateFactualIntegrity(original, keywords, suggestion));
        assertTrue(ex.getMessage().contains("Fabricated proper noun or term 'AWS'"));
    }

    @Test
    void testValidate_Success_SentenceStartCapitalizationBypass() {
        String original = "improved code speed.";
        // Suggestion starts with capitalized non-resume word, but not fully uppercase
        String suggestion = "Optimized application speed.";
        List<String> keywords = List.of("application");

        assertDoesNotThrow(() -> service.validateFactualIntegrity(original, keywords, suggestion));
    }

    @Test
    void testValidate_Failure_FullyUppercaseSentenceStartProperNoun() {
        String original = "code development.";
        // AWS is fully uppercase, represents a fabricated skill/proper noun even if it's first word
        String suggestion = "AWS development engineered.";
        List<String> keywords = List.of("development");

        FactualValidationException ex = assertThrows(FactualValidationException.class,
                () -> service.validateFactualIntegrity(original, keywords, suggestion));
        assertTrue(ex.getMessage().contains("Fabricated proper noun or term 'AWS'"));
    }

    @Test
    void testValidate_NullInputs_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> service.validateFactualIntegrity(null, List.of(), "test"));
        assertThrows(IllegalArgumentException.class, () -> service.validateFactualIntegrity("test", List.of(), null));
    }
}
