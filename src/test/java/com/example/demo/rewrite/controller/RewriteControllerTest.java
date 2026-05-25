package com.example.demo.rewrite.controller;

import com.example.demo.ai.validation.FactualValidationException;
import com.example.demo.rewrite.dto.RewriteRequest;
import com.example.demo.rewrite.dto.RewriteResponseDto;
import com.example.demo.rewrite.service.RewriteService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.demo.controller.GlobalExceptionHandler;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RewriteControllerTest {

    @Mock
    private RewriteService rewriteService;

    @InjectMocks
    private RewriteController rewriteController;

    private MockMvc mockMvc;
    private Validator validator;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(rewriteController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testRewriteSection_Success() throws Exception {
        UUID docId = UUID.randomUUID();
        String originalText = "Led software team.";
        List<String> keywords = List.of("software", "team");
        RewriteRequest request = new RewriteRequest(docId, originalText, keywords);

        RewriteResponseDto responseDto = new RewriteResponseDto(
                originalText,
                List.of(new RewriteResponseDto.SuggestionDto("ACTION_ORIENTED", "Managed high performance software team."))
        );

        when(rewriteService.rewriteSection(docId, originalText, keywords)).thenReturn(responseDto);

        mockMvc.perform(post("/api/rewrite/section")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.originalText").value(originalText))
                .andExpect(jsonPath("$.suggestions[0].style").value("ACTION_ORIENTED"))
                .andExpect(jsonPath("$.suggestions[0].text").value("Managed high performance software team."));
    }

    @Test
    void testRewriteSection_FactualValidationError_Returns422() throws Exception {
        UUID docId = UUID.randomUUID();
        String originalText = "Led software team.";
        List<String> keywords = List.of("software", "team");
        RewriteRequest request = new RewriteRequest(docId, originalText, keywords);

        when(rewriteService.rewriteSection(any(), any(), any()))
                .thenThrow(new FactualValidationException("Hallucination detected: Fabricated proper noun 'AWS'."));

        mockMvc.perform(post("/api/rewrite/section")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Hallucination detected: Fabricated proper noun 'AWS'."));
    }

    @Test
    void testValidation_NullDocumentId_HasViolation() {
        RewriteRequest request = new RewriteRequest(null, "Some text", List.of());
        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("documentId must not be null")));
    }

    @Test
    void testValidation_BlankOriginalText_HasViolation() {
        RewriteRequest request = new RewriteRequest(UUID.randomUUID(), "   ", List.of());
        var violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("originalText must not be blank")));
    }

    @Test
    void testValidation_ValidRequest_NoViolations() {
        RewriteRequest request = new RewriteRequest(UUID.randomUUID(), "Led team.", List.of());
        var violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }
}
