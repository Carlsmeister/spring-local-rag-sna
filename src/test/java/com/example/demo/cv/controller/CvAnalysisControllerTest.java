package com.example.demo.cv.controller;

import com.example.demo.cv.dto.CvAnalysisRequest;
import com.example.demo.cv.dto.CvAnalysisResponseDto;
import com.example.demo.cv.service.CvAnalysisService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CvAnalysisControllerTest {

    @Mock
    private CvAnalysisService cvAnalysisService;

    @InjectMocks
    private CvAnalysisController cvAnalysisController;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testAnalyzeCv_Success() {
        UUID docId = UUID.randomUUID();
        UUID analysisId = UUID.randomUUID();
        CvAnalysisResponseDto expectedResponse = new CvAnalysisResponseDto(
                analysisId,
                docId,
                80,
                List.of("Strong layout"),
                List.of("Formatting weaknesses"),
                List.of("Improve margins"),
                LocalDateTime.now()
        );

        when(cvAnalysisService.analyzeCv(docId)).thenReturn(expectedResponse);

        CvAnalysisRequest request = new CvAnalysisRequest(docId);
        ResponseEntity<CvAnalysisResponseDto> response = cvAnalysisController.analyzeCv(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testAnalyzeCv_NotFound_PropagatesException() {
        UUID docId = UUID.randomUUID();
        when(cvAnalysisService.analyzeCv(docId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found"));

        CvAnalysisRequest request = new CvAnalysisRequest(docId);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> cvAnalysisController.analyzeCv(request));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Document not found", ex.getReason());
    }

    @Test
    void testValidation_NullDocumentId_HasViolation() {
        CvAnalysisRequest request = new CvAnalysisRequest(null);
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Validation violations should not be empty for null documentId");
        assertEquals("documentId must not be null", violations.iterator().next().getMessage());
    }

    @Test
    void testValidation_ValidRequest_NoViolations() {
        CvAnalysisRequest request = new CvAnalysisRequest(UUID.randomUUID());
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Validation violations should be empty for valid request");
    }
}
