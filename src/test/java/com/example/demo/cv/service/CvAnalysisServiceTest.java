package com.example.demo.cv.service;

import com.example.demo.cv.dto.CvAnalysisResponseDto;
import com.example.demo.cv.model.CvAnalysis;
import com.example.demo.cv.repository.CvAnalysisRepository;
import com.example.demo.dto.CvAnalysisResponse;
import com.example.demo.ai.workflow.WorkflowOrchestrator;
import com.example.demo.model.Document;
import com.example.demo.model.ExtractedText;
import com.example.demo.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CvAnalysisServiceTest {

    private DocumentRepository documentRepository;
    private CvAnalysisRepository cvAnalysisRepository;
    private WorkflowOrchestrator workflowOrchestrator;
    private CvAnalysisService cvAnalysisService;

    @BeforeEach
    void setUp() {
        documentRepository = mock(DocumentRepository.class);
        cvAnalysisRepository = mock(CvAnalysisRepository.class);
        workflowOrchestrator = mock(WorkflowOrchestrator.class);

        cvAnalysisService = new CvAnalysisService(documentRepository, cvAnalysisRepository, workflowOrchestrator);
    }

    @Test
    void testAnalyzeCv_Success() {
        UUID docId = UUID.randomUUID();
        Document document = Document.builder()
                .id(docId)
                .fileName("resume.pdf")
                .fileSize(100L)
                .contentType("application/pdf")
                .createdAt(LocalDateTime.now())
                .build();
        
        ExtractedText extractedText = ExtractedText.builder()
                .document(document)
                .rawContent("Hello <World> from Java!")
                .parsedAt(LocalDateTime.now())
                .build();
        
        document.setExtractedText(extractedText);

        when(documentRepository.findById(docId)).thenReturn(Optional.of(document));

        CvAnalysisResponse parsedResponse = new CvAnalysisResponse(
                85,
                List.of("Strength 1"),
                List.of("Weakness 1"),
                List.of("Rec 1")
        );
        WorkflowOrchestrator.WorkflowResult mockResult = new WorkflowOrchestrator.WorkflowResult(
                parsedResponse,
                "mock-raw-response"
        );
        when(workflowOrchestrator.orchestrate(anyString())).thenReturn(mockResult);

        UUID analysisId = UUID.randomUUID();
        when(cvAnalysisRepository.save(any(CvAnalysis.class))).thenAnswer(invocation -> {
            CvAnalysis cvAnalysis = invocation.getArgument(0);
            cvAnalysis.setId(analysisId);
            cvAnalysis.setAnalyzedAt(LocalDateTime.now());
            return cvAnalysis;
        });

        CvAnalysisResponseDto result = cvAnalysisService.analyzeCv(docId);

        assertNotNull(result);
        assertEquals(docId, result.documentId());
        assertEquals(analysisId, result.analysisId());
        assertEquals(85, result.atsScore());
        assertEquals(List.of("Strength 1"), result.strengths());
        assertEquals(List.of("Weakness 1"), result.weaknesses());
        assertEquals(List.of("Rec 1"), result.recommendations());
        assertNotNull(result.analyzedAt());

        // Verify escaping: "<World>" should become "[World]" in orchestrator payload
        ArgumentCaptor<String> textCaptor = ArgumentCaptor.forClass(String.class);
        verify(workflowOrchestrator).orchestrate(textCaptor.capture());
        String capturedText = textCaptor.getValue();
        assertTrue(capturedText.contains("[World]"));
        assertFalse(capturedText.contains("<World>"));
    }

    @Test
    void testAnalyzeCv_DocumentNotFound_ThrowsException() {
        UUID docId = UUID.randomUUID();
        when(documentRepository.findById(docId)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> cvAnalysisService.analyzeCv(docId));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertTrue(ex.getReason().contains("Document not found"));
    }
}
