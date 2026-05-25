package com.example.demo.job.service;

import com.example.demo.job.dto.JobMatchAiResponse;
import com.example.demo.job.dto.JobMatchResponseDto;
import com.example.demo.job.model.JobAd;
import com.example.demo.job.model.JobMatchResult;
import com.example.demo.job.repository.JobAdRepository;
import com.example.demo.job.repository.JobMatchResultRepository;
import com.example.demo.ai.rag.service.EmbeddingService;
import com.example.demo.mapper.JobMatchMapper;
import com.example.demo.model.Document;
import com.example.demo.repository.DocumentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class JobMatchingService {

    private final DocumentRepository documentRepository;
    private final JobAdRepository jobAdRepository;
    private final JobMatchResultRepository jobMatchResultRepository;
    private final EmbeddingService embeddingService;
    private final ChatClient chatClient;
    private final JobMatchMapper jobMatchMapper;
    private final ObjectMapper objectMapper;

    @Value("classpath:prompts/job-matching-system-prompt.txt")
    private Resource systemPromptResource;

    public JobMatchingService(
            DocumentRepository documentRepository,
            JobAdRepository jobAdRepository,
            JobMatchResultRepository jobMatchResultRepository,
            EmbeddingService embeddingService,
            ChatClient.Builder chatClientBuilder,
            JobMatchMapper jobMatchMapper) {
        this.documentRepository = documentRepository;
        this.jobAdRepository = jobAdRepository;
        this.jobMatchResultRepository = jobMatchResultRepository;
        this.embeddingService = embeddingService;
        this.chatClient = chatClientBuilder.build();
        this.jobMatchMapper = jobMatchMapper;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Performs semantic analysis matching CV sections against target job descriptions,
     * persists metadata results, and returns actionable match metrics.
     *
     * @param documentId the ID of the candidate's CV document
     * @param jobAdText the text block of the job advertisement description
     * @return the JobMatchResponseDto containing match percentage, missing keywords, and suggestions
     */
    @Transactional
    public JobMatchResponseDto matchJobAd(UUID documentId, String jobAdText) {
        if (documentId == null) {
            throw new IllegalArgumentException("documentId must not be null");
        }
        if (jobAdText == null || jobAdText.isBlank()) {
            throw new IllegalArgumentException("jobAdText must not be null or blank");
        }

        // 1. Fetch document from the database
        var documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with ID: " + documentId);
        }
        Document document = documentOpt.get();

        // 2. Fetch top 5 relevant CV chunks via semantic search
        List<org.springframework.ai.document.Document> chunks = embeddingService.searchChunks(documentId, jobAdText, 5);

        // 3. Escape XML tags defensively in all prompt inputs to prevent injection attacks
        String mergedChunks = chunks.stream()
                .map(org.springframework.ai.document.Document::getText)
                .map(this::escapeXmlTags)
                .collect(Collectors.joining("\n\n"));

        String sanitizedJobAd = escapeXmlTags(jobAdText);

        // 4. Load system prompt from classpath resource
        String systemPrompt;
        try (var inputStream = systemPromptResource.getInputStream()) {
            systemPrompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read job matching system prompt from classpath resource", e);
        }

        // 5. Construct candidate and job ad payload tags
        String userPayload = "<candidate_resume_payload>\n" + mergedChunks + "\n</candidate_resume_payload>\n" +
                            "<job_ad_payload>\n" + sanitizedJobAd + "\n</job_ad_payload>";

        // 6. Query the local ChatModel
        String rawResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPayload)
                .call()
                .content();

        // 7. Parse response with Mapper
        JobMatchAiResponse aiResponse = jobMatchMapper.toJobMatchAiResponse(rawResponse);

        // 8. Serialize missing keywords to JSON list format
        String missingKeywordsJson;
        try {
            missingKeywordsJson = objectMapper.writeValueAsString(aiResponse.missingKeywords());
        } catch (Exception e) {
            missingKeywordsJson = "[]";
        }

        // 9. Save JobAd to the database
        String title = jobAdText.substring(0, Math.min(jobAdText.length(), 100)).trim();
        JobAd jobAd = JobAd.builder()
                .title(title)
                .rawText(jobAdText)
                .userProfile(document.getUserProfile())
                .build();
        JobAd savedJobAd = jobAdRepository.save(jobAd);

        // 10. Save JobMatchResult to the database
        JobMatchResult matchResult = JobMatchResult.builder()
                .document(document)
                .jobAd(savedJobAd)
                .matchScore(aiResponse.matchPercentage())
                .missingKeywords(missingKeywordsJson)
                .rawAnalysis(rawResponse)
                .build();
        jobMatchResultRepository.save(matchResult);

        // 11. Return response DTO
        return new JobMatchResponseDto(
                aiResponse.matchPercentage(),
                aiResponse.missingKeywords(),
                aiResponse.matchingStrength(),
                aiResponse.actionableSuggestions()
        );
    }

    private String escapeXmlTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("<", "[").replace(">", "]");
    }
}
