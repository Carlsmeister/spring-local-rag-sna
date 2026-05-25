package com.example.demo.ai.workflow.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class AiMetricsTracker {

    private static final Logger log = LoggerFactory.getLogger("com.example.demo.ai.metrics");
    private final List<WorkflowMetric> metricsHistory = new CopyOnWriteArrayList<>();
    private final ObjectMapper objectMapper;
    private final String summaryFilePath = "target/ai-metrics/summary.json";

    public AiMetricsTracker() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public void registerMetric(WorkflowMetric metric) {
        if (metric == null) {
            return;
        }

        metricsHistory.add(metric);

        // 1. Log as structured JSON Line
        try {
            String jsonLine = objectMapper.writeValueAsString(metric).replace("\n", "").replace("\r", "").trim();
            log.info("{}", jsonLine);
        } catch (Exception e) {
            log.warn("Failed to serialize workflow metric to JSON for logging", e);
        }

        // 2. Periodically write summaries
        saveSummaryReport();
    }

    public List<WorkflowMetric> getMetricsHistory() {
        return Collections.unmodifiableList(metricsHistory);
    }

    public void clearHistory() {
        metricsHistory.clear();
        saveSummaryReport();
    }

    public Map<String, Object> calculateAggregates() {
        Map<String, Object> stats = new LinkedHashMap<>();
        int totalRuns = metricsHistory.size();
        stats.put("totalRuns", totalRuns);

        if (totalRuns == 0) {
            stats.put("successRate", 0.0);
            stats.put("avgLatencyMs", 0.0);
            stats.put("p95LatencyMs", 0.0);
            stats.put("totalTokensConsumed", 0L);
            return stats;
        }

        long successCount = metricsHistory.stream().filter(WorkflowMetric::success).count();
        double successRate = (double) successCount / totalRuns * 100.0;
        stats.put("successRate", successRate);

        double avgLatency = metricsHistory.stream()
                .mapToLong(WorkflowMetric::totalLatencyMs)
                .average()
                .orElse(0.0);
        stats.put("avgLatencyMs", avgLatency);

        // P95 Latency Calculation
        List<Long> latencies = metricsHistory.stream()
                .map(WorkflowMetric::totalLatencyMs)
                .sorted()
                .toList();
        int p95Index = (int) Math.ceil(0.95 * latencies.size()) - 1;
        long p95Latency = latencies.get(Math.max(0, Math.min(p95Index, latencies.size() - 1)));
        stats.put("p95LatencyMs", p95Latency);

        // Token aggregates
        long totalPromptTokens = 0;
        long totalGenTokens = 0;
        long totalWorkflowTokens = 0;
        int totalRejections = 0;

        Map<String, Long> agentTotalLatency = new HashMap<>();
        Map<String, Long> agentTotalTokens = new HashMap<>();
        Map<String, Integer> agentCounts = new HashMap<>();

        for (WorkflowMetric m : metricsHistory) {
            totalRejections += m.validationRejectCount();
            for (AgentMetric am : m.agentMetrics()) {
                totalPromptTokens += am.promptTokens() != null ? am.promptTokens() : 0;
                totalGenTokens += am.generationTokens() != null ? am.generationTokens() : 0;
                totalWorkflowTokens += am.totalTokens() != null ? am.totalTokens() : 0;

                agentTotalLatency.merge(am.agentName(), am.latencyMs(), Long::sum);
                agentTotalTokens.merge(am.agentName(), am.totalTokens() != null ? am.totalTokens() : 0, Long::sum);
                agentCounts.merge(am.agentName(), 1, Integer::sum);
            }
        }

        stats.put("totalPromptTokens", totalPromptTokens);
        stats.put("totalGenerationTokens", totalGenTokens);
        stats.put("totalTokensConsumed", totalWorkflowTokens);
        stats.put("avgValidationRejectionsPerRun", (double) totalRejections / totalRuns);

        // Sub-agent breakdowns
        Map<String, Map<String, Object>> agentBreakdowns = new LinkedHashMap<>();
        for (String agent : agentTotalLatency.keySet()) {
            int count = agentCounts.getOrDefault(agent, 1);
            Map<String, Object> agentStats = new LinkedHashMap<>();
            agentStats.put("avgLatencyMs", (double) agentTotalLatency.get(agent) / count);
            agentStats.put("avgTokensConsumed", (double) agentTotalTokens.get(agent) / count);
            agentBreakdowns.put(agent, agentStats);
        }
        stats.put("agentBreakdowns", agentBreakdowns);

        return stats;
    }

    private synchronized void saveSummaryReport() {
        try {
            Map<String, Object> stats = calculateAggregates();
            File summaryFile = new File(summaryFilePath);
            File parentDir = summaryFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            objectMapper.writeValue(summaryFile, stats);
        } catch (IOException e) {
            // Keep logging PII-free internally
            System.err.println("Failed to write metrics summary report to file: " + e.getMessage());
        }
    }
}
