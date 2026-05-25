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
public class ValidationAgent {

    private final ChatClient chatClient;

    @Value("classpath:prompts/validation-agent-system-prompt.txt")
    private Resource systemPromptResource;

    public ValidationAgent(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public AgentResponse validateAndConsolidate(
            String sanitizedText,
            String atsReport,
            String recruiterReport,
            String keywordReport) {

        if (sanitizedText == null) {
            sanitizedText = "";
        }
        if (atsReport == null) {
            atsReport = "";
        }
        if (recruiterReport == null) {
            recruiterReport = "";
        }
        if (keywordReport == null) {
            keywordReport = "";
        }

        String systemPrompt;
        try (var inputStream = systemPromptResource.getInputStream()) {
            systemPrompt = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return AgentResponse.failure("Failed to read Validation agent system prompt: " + e.getMessage());
        }

        String userPayload = "<candidate_resume_payload>\n" + sanitizedText + "\n</candidate_resume_payload>\n\n"
                + "<agent_reports_payload>\n"
                + "<ats_report>\n" + atsReport + "\n</ats_report>\n"
                + "<recruiter_report>\n" + recruiterReport + "\n</recruiter_report>\n"
                + "<keyword_report>\n" + keywordReport + "\n</keyword_report>\n"
                + "</agent_reports_payload>";

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
            return AgentResponse.failure("Validation Agent LLM call failed: " + e.getMessage());
        }
    }
}
