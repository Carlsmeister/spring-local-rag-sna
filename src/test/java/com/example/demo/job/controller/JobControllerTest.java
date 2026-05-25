package com.example.demo.job.controller;

import com.example.demo.job.dto.JobMatchRequest;
import com.example.demo.job.dto.JobMatchResponseDto;
import com.example.demo.job.service.JobMatchingService;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobControllerTest {

    @Mock
    private JobMatchingService jobMatchingService;

    @InjectMocks
    private JobController jobController;

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testMatchJob_Success() {
        UUID docId = UUID.randomUUID();
        String jobAdText = "Seeking Spring Boot developer.";
        JobMatchRequest request = new JobMatchRequest(docId, jobAdText);

        JobMatchResponseDto expectedResponse = new JobMatchResponseDto(
                85,
                List.of("Kubernetes"),
                "High match",
                List.of("Add Kubernetes details")
        );

        when(jobMatchingService.matchJobAd(docId, jobAdText)).thenReturn(expectedResponse);

        ResponseEntity<JobMatchResponseDto> response = jobController.matchJob(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(expectedResponse, response.getBody());
    }

    @Test
    void testValidation_NullDocumentId_HasViolation() {
        JobMatchRequest request = new JobMatchRequest(null, "Seeking developer.");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Validation violations should not be empty for null documentId");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")),
                "Should have null validation error");
    }

    @Test
    void testValidation_BlankJobAdText_HasViolation() {
        JobMatchRequest request = new JobMatchRequest(UUID.randomUUID(), "   ");
        var violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Validation violations should not be empty for blank jobAdText");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("required")),
                "Should have blank validation error");
    }

    @Test
    void testValidation_ValidRequest_NoViolations() {
        JobMatchRequest request = new JobMatchRequest(UUID.randomUUID(), "Seeking Spring Boot developer.");
        var violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Validation violations should be empty for valid request");
    }
}
