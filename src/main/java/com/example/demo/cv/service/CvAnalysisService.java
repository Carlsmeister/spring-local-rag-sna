package com.example.demo.cv.service;

import com.example.demo.cv.dto.CvAnalysisResponseDto;
import com.example.demo.cv.model.CvAnalysis;
import com.example.demo.cv.repository.CvAnalysisRepository;
import com.example.demo.dto.CvAnalysisResponse;
import com.example.demo.ai.workflow.WorkflowOrchestrator;
import com.example.demo.ai.workflow.WorkflowOrchestrator.WorkflowResult;
import com.example.demo.model.Document;
import com.example.demo.model.ExtractedText;
import com.example.demo.repository.DocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class CvAnalysisService {

    private final DocumentRepository documentRepository;
    private final CvAnalysisRepository cvAnalysisRepository;
    private final WorkflowOrchestrator workflowOrchestrator;

    public CvAnalysisService(
            DocumentRepository documentRepository,
            CvAnalysisRepository cvAnalysisRepository,
            WorkflowOrchestrator workflowOrchestrator) {
        this.documentRepository = documentRepository;
        this.cvAnalysisRepository = cvAnalysisRepository;
        this.workflowOrchestrator = workflowOrchestrator;
    }

    @Transactional
    public CvAnalysisResponseDto analyzeCv(UUID documentId) {
        if (documentId == null) {
            throw new IllegalArgumentException("documentId must not be null");
        }

        var documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with ID: " + documentId);
        }

        Document document = documentOpt.get();
        ExtractedText extractedTextEntity = document.getExtractedText();
        String rawText = extractedTextEntity != null ? extractedTextEntity.getRawContent() : "";
        if (rawText == null) {
            rawText = "";
        }

        // Defensive escaping of prompt injection tags
        String sanitizedText = rawText.replace("<", "[").replace(">", "]");

        // Run multi-agent orchestrator instead of a single LLM prompt
        WorkflowResult result = workflowOrchestrator.orchestrate(sanitizedText);
        CvAnalysisResponse cvResponse = result.response();
        String rawResponse = result.rawResponse();

        // Save analysis to database
        CvAnalysis cvAnalysis = CvAnalysis.builder()
                .document(document)
                .atsScore(cvResponse.atsScore())
                .rawAiResponse(rawResponse)
                .analyzedAt(LocalDateTime.now())
                .build();

        CvAnalysis savedAnalysis = cvAnalysisRepository.save(cvAnalysis);

        // Map to API DTO contract
        return new CvAnalysisResponseDto(
                savedAnalysis.getId(),
                documentId,
                cvResponse.atsScore(),
                cvResponse.strengths(),
                cvResponse.weaknesses(),
                cvResponse.recommendations(),
                savedAnalysis.getAnalyzedAt()
        );
    }
}
