package com.example.demo.ai.workflow.metrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record WorkflowMetric(
        UUID correlationId,
        long totalLatencyMs,
        boolean success,
        Integer finalAtsScore,
        int validationRejectCount,
        List<AgentMetric> agentMetrics,
        LocalDateTime timestamp
) {}
