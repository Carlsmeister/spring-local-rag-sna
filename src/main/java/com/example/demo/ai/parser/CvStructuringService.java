package com.example.demo.ai.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class CvStructuringService {

    private static final Logger log = LoggerFactory.getLogger(CvStructuringService.class);
    private final ChatClient chatClient;

    @Value("classpath:prompts/cv-structuring-prompt.txt")
    private Resource systemPromptResource;

    public CvStructuringService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    /**
     * Reorganizes raw layout-scrambled CV text into structured Markdown.
     *
     * @param rawText the raw text extracted from the document
     * @return clean structured markdown text
     */
    public String structureCv(String rawText) {
        if (rawText == null || rawText.strip().isEmpty()) {
            return "";
        }

        String systemPrompt;
        try (var inputStream = systemPromptResource.getInputStream()) {
            systemPrompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read CV structuring prompt template: {}", e.getMessage());
            return rawText; // Fallback to raw text if template reading fails
        }

        // Escape tags defensively to prevent prompt injection attacks
        String sanitizedText = rawText.replace("<", "[").replace(">", "]");
        String userPayload = "<raw_scrambled_resume>\n" + sanitizedText + "\n</raw_scrambled_resume>";

        try {
            log.info("Sending raw CV text to Ollama for layout restructuring...");
            ChatResponse chatResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPayload)
                    .call()
                    .chatResponse();

            if (chatResponse != null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                String structuredText = chatResponse.getResult().getOutput().getText();
                if (structuredText != null && !structuredText.strip().isEmpty()) {
                    log.info("Successfully restructured CV layout via local LLM.");
                    return structuredText.trim();
                }
            }
            log.warn("Empty response received from local LLM during restructuring, falling back to raw text.");
            return rawText;
        } catch (Exception e) {
            log.error("Failed to restructure CV layout via local LLM: {}", e.getMessage(), e);
            return rawText; // Fallback safely to raw text
        }
    }
}
