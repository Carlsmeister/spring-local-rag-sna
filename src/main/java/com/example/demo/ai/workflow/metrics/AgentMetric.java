package com.example.demo.ai.workflow.metrics;

public record AgentMetric(
        String agentName,
        long latencyMs,
        Long promptTokens,
        Long generationTokens,
        Long totalTokens,
        boolean success,
        String errorMessage
) {}
