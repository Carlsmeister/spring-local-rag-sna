package com.example.demo.ai.workflow.metrics;

public record AgentResponse(
        String content,
        Long promptTokens,
        Long generationTokens,
        Long totalTokens,
        boolean success,
        String errorMessage
) {
    public static AgentResponse success(String content, Long promptTokens, Long generationTokens, Long totalTokens) {
        return new AgentResponse(content, promptTokens, generationTokens, totalTokens, true, null);
    }

    public static AgentResponse failure(String errorMessage) {
        return new AgentResponse(null, 0L, 0L, 0L, false, errorMessage);
    }
}
