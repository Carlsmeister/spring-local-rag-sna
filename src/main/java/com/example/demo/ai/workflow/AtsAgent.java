package com.example.demo.ai.workflow;

import com.example.demo.ai.workflow.metrics.AgentResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AtsAgent {

    private final ChatClient chatClient;

    @Value("classpath:prompts/ats-agent-system-prompt.txt")
    private Resource systemPromptResource;

    public AtsAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AgentResponse analyze(String sanitizedText) {
        if (sanitizedText == null || sanitizedText.isBlank()) {
            return AgentResponse.success("ATS Agent Report: No CV text provided.", 0L, 0L, 0L);
        }

        String systemPrompt;
        try (var inputStream = systemPromptResource.getInputStream()) {
            systemPrompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return AgentResponse.failure("Failed to read ATS agent system prompt: " + e.getMessage());
        }

        String userPayload = "<candidate_resume_payload>\n" + sanitizedText + "\n</candidate_resume_payload>";

        try {
            ChatResponse chatResponse = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPayload)
                    .call()
                    .chatResponse();

            String report = "";
            if (chatResponse != null && chatResponse.getResult() != null && chatResponse.getResult().getOutput() != null) {
                report = chatResponse.getResult().getOutput().getText();
            }

            Long promptTokens = 0L;
            Long generationTokens = 0L;
            Long totalTokens = 0L;

            if (chatResponse != null && chatResponse.getMetadata() != null && chatResponse.getMetadata().getUsage() != null) {
                var usage = chatResponse.getMetadata().getUsage();
                promptTokens = usage.getPromptTokens() != null ? usage.getPromptTokens().longValue() : 0L;
                generationTokens = usage.getCompletionTokens() != null ? usage.getCompletionTokens().longValue() : 0L;
                totalTokens = usage.getTotalTokens() != null ? usage.getTotalTokens().longValue() : 0L;
            }

            return AgentResponse.success(report, promptTokens, generationTokens, totalTokens);
        } catch (Exception e) {
            return AgentResponse.failure("ATS Agent LLM call failed: " + e.getMessage());
        }
    }
}
