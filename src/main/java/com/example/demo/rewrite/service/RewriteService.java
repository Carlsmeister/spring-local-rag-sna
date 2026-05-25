package com.example.demo.rewrite.service;

import com.example.demo.ai.validation.FactualIntegrityService;
import com.example.demo.mapper.RewriteMapper;
import com.example.demo.model.Document;
import com.example.demo.repository.DocumentRepository;
import com.example.demo.rewrite.dto.RewriteAiResponse;
import com.example.demo.rewrite.dto.RewriteResponseDto;
import com.example.demo.rewrite.model.RewriteStyle;
import com.example.demo.rewrite.model.RewriteSuggestion;
import com.example.demo.rewrite.repository.RewriteSuggestionRepository;
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
public class RewriteService {

    private final DocumentRepository documentRepository;
    private final RewriteSuggestionRepository rewriteSuggestionRepository;
    private final FactualIntegrityService factualIntegrityService;
    private final ChatClient chatClient;
    private final RewriteMapper rewriteMapper;

    @Value("classpath:prompts/section-rewrite-system-prompt.txt")
    private Resource systemPromptResource;

    public RewriteService(
            DocumentRepository documentRepository,
            RewriteSuggestionRepository rewriteSuggestionRepository,
            FactualIntegrityService factualIntegrityService,
            ChatClient.Builder chatClientBuilder,
            RewriteMapper rewriteMapper) {
        this.documentRepository = documentRepository;
        this.rewriteSuggestionRepository = rewriteSuggestionRepository;
        this.factualIntegrityService = factualIntegrityService;
        this.chatClient = chatClientBuilder.build();
        this.rewriteMapper = rewriteMapper;
    }

    /**
     * Generates factual, style-optimized sentence suggestions, audits them against factual guardrails,
     * persists cached suggestion matches, and returns response metrics.
     *
     * @param documentId the ID of the candidate's CV document
     * @param originalText the sentence or bullet point to optimize
     * @param keywords the list of keywords to incorporate
     * @return the RewriteResponseDto containing the styles and optimized texts
     */
    @Transactional
    public RewriteResponseDto rewriteSection(UUID documentId, String originalText, List<String> keywords) {
        if (documentId == null) {
            throw new IllegalArgumentException("documentId must not be null");
        }
        if (originalText == null || originalText.isBlank()) {
            throw new IllegalArgumentException("originalText must not be null or blank");
        }

        // 1. Fetch document from the database to check existence
        var documentOpt = documentRepository.findById(documentId);
        if (documentOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Document not found with ID: " + documentId);
        }
        Document document = documentOpt.get();

        // 2. Escape XML tags defensively in all prompt inputs to prevent injection attacks
        String sanitizedOriginalText = escapeXmlTags(originalText);
        String sanitizedKeywords = keywords != null ? keywords.stream()
                .map(this::escapeXmlTags)
                .collect(Collectors.joining(", ")) : "";

        // 3. Load decoupled system prompt from classpath resource
        String systemPrompt;
        try (var inputStream = systemPromptResource.getInputStream()) {
            systemPrompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read section rewrite system prompt from classpath resource", e);
        }

        // 4. Construct candidate and keyword payload tags
        String userPayload = "<original_sentence>\n" + sanitizedOriginalText + "\n</original_sentence>\n" +
                             "<target_keywords>\n" + sanitizedKeywords + "\n</target_keywords>";

        // 5. Query the local ChatModel
        String rawResponse = chatClient.prompt()
                .system(systemPrompt)
                .user(userPayload)
                .call()
                .content();

        // 6. Parse response with Mapper
        RewriteAiResponse aiResponse = rewriteMapper.toRewriteAiResponse(rawResponse);

        // 7. Validate factual integrity of all suggested bullet points
        if (aiResponse.suggestions() != null) {
            for (var sug : aiResponse.suggestions()) {
                factualIntegrityService.validateFactualIntegrity(originalText, keywords, sug.text());
            }
        }

        // 8. Convert suggestion records, cache/persist to DB, and assemble response
        List<RewriteResponseDto.SuggestionDto> suggestionDtos = aiResponse.suggestions().stream()
                .map(sug -> {
                    RewriteStyle style;
                    try {
                        style = RewriteStyle.valueOf(sug.style());
                    } catch (IllegalArgumentException e) {
                        // Fallback in case the model returns adjacent styles
                        style = RewriteStyle.ACTION_ORIENTED;
                    }

                    // Save Suggestion to DB
                    RewriteSuggestion suggestionEntity = RewriteSuggestion.builder()
                            .document(document)
                            .originalSentence(originalText)
                            .suggestedStyle(style)
                            .suggestedText(sug.text())
                            .build();
                    rewriteSuggestionRepository.save(suggestionEntity);

                    return new RewriteResponseDto.SuggestionDto(sug.style(), sug.text());
                })
                .collect(Collectors.toList());

        return new RewriteResponseDto(originalText, suggestionDtos);
    }

    private String escapeXmlTags(String text) {
        if (text == null) {
            return "";
        }
        return text.replace("<", "[").replace(">", "]");
    }
}
