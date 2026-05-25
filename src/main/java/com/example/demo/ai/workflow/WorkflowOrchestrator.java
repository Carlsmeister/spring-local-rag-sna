package com.example.demo.ai.workflow;

import com.example.demo.dto.CvAnalysisResponse;
import com.example.demo.mapper.CvAnalysisMapper;
import com.example.demo.ai.workflow.metrics.*;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
public class WorkflowOrchestrator {

    private final AtsAgent atsAgent;
    private final RecruiterAgent recruiterAgent;
    private final KeywordAgent keywordAgent;
    private final ValidationAgent validationAgent;
    private final CvAnalysisMapper cvAnalysisMapper;
    private final AiMetricsTracker aiMetricsTracker;

    // Dedicated executor pool to avoid blocking ForkJoinPool.commonPool
    private final ExecutorService executorService;

    public record WorkflowResult(
            CvAnalysisResponse response,
            String rawResponse
    ) {}

    private record AgentExecutionResult(
            AgentResponse response,
            AgentMetric metric
    ) {}

    public WorkflowOrchestrator(
            AtsAgent atsAgent,
            RecruiterAgent recruiterAgent,
            KeywordAgent keywordAgent,
            ValidationAgent validationAgent,
            CvAnalysisMapper cvAnalysisMapper,
            AiMetricsTracker aiMetricsTracker) {
        this.atsAgent = atsAgent;
        this.recruiterAgent = recruiterAgent;
        this.keywordAgent = keywordAgent;
        this.validationAgent = validationAgent;
        this.cvAnalysisMapper = cvAnalysisMapper;
        this.aiMetricsTracker = aiMetricsTracker;

        // 3 parallel threads corresponding to our 3 concurrent sub-agents
        this.executorService = Executors.newFixedThreadPool(3, r -> {
            Thread thread = new Thread(r);
            thread.setName("ai-workflow-agent-pool-" + thread.getId());
            thread.setDaemon(true);
            return thread;
        });
    }

    public WorkflowResult orchestrate(String sanitizedText) {
        if (sanitizedText == null) {
            sanitizedText = "";
        }

        final String finalSanitizedText = sanitizedText;
        long startTime = System.currentTimeMillis();
        UUID correlationId = UUID.randomUUID();

        // 1. Trigger parallel sub-agents (ATS, Recruiter, Keyword)
        CompletableFuture<AgentExecutionResult> atsFuture = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            AgentResponse res = atsAgent.analyze(finalSanitizedText);
            long latency = System.currentTimeMillis() - start;
            AgentMetric metric = new AgentMetric("ATS Agent", latency, res.promptTokens(), res.generationTokens(), res.totalTokens(), res.success(), res.errorMessage());
            return new AgentExecutionResult(res, metric);
        }, executorService);

        CompletableFuture<AgentExecutionResult> recruiterFuture = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            AgentResponse res = recruiterAgent.analyze(finalSanitizedText);
            long latency = System.currentTimeMillis() - start;
            AgentMetric metric = new AgentMetric("Recruiter Agent", latency, res.promptTokens(), res.generationTokens(), res.totalTokens(), res.success(), res.errorMessage());
            return new AgentExecutionResult(res, metric);
        }, executorService);

        CompletableFuture<AgentExecutionResult> keywordFuture = CompletableFuture.supplyAsync(() -> {
            long start = System.currentTimeMillis();
            AgentResponse res = keywordAgent.analyze(finalSanitizedText);
            long latency = System.currentTimeMillis() - start;
            AgentMetric metric = new AgentMetric("Keyword Agent", latency, res.promptTokens(), res.generationTokens(), res.totalTokens(), res.success(), res.errorMessage());
            return new AgentExecutionResult(res, metric);
        }, executorService);

        // Wait for all three parallel agents to finish (timeout at 180 seconds for safety)
        try {
            CompletableFuture.allOf(atsFuture, recruiterFuture, keywordFuture)
                    .get(180, TimeUnit.SECONDS);
        } catch (Exception e) {
            WorkflowMetric errorMetric = new WorkflowMetric(
                    correlationId,
                    System.currentTimeMillis() - startTime,
                    false,
                    null,
                    0,
                    List.of(
                            new AgentMetric("ATS Agent", 0, 0L, 0L, 0L, false, "Workflow timed out"),
                            new AgentMetric("Recruiter Agent", 0, 0L, 0L, 0L, false, "Workflow timed out"),
                            new AgentMetric("Keyword Agent", 0, 0L, 0L, 0L, false, "Workflow timed out"),
                            new AgentMetric("Validation Agent", 0, 0L, 0L, 0L, false, "Parent execution timed out")
                    ),
                    LocalDateTime.now()
            );
            aiMetricsTracker.registerMetric(errorMetric);
            throw new RuntimeException("AI Workflow timed out or failed during parallel agent execution", e);
        }

        AgentExecutionResult atsRes;
        AgentExecutionResult recruiterRes;
        AgentExecutionResult keywordRes;

        try {
            atsRes = atsFuture.get();
            recruiterRes = recruiterFuture.get();
            keywordRes = keywordFuture.get();
        } catch (Exception e) {
            WorkflowMetric errorMetric = new WorkflowMetric(
                    correlationId,
                    System.currentTimeMillis() - startTime,
                    false,
                    null,
                    0,
                    List.of(
                            new AgentMetric("ATS Agent", 0, 0L, 0L, 0L, false, "Failed to retrieve results: " + e.getMessage()),
                            new AgentMetric("Recruiter Agent", 0, 0L, 0L, 0L, false, "Failed to retrieve results: " + e.getMessage()),
                            new AgentMetric("Keyword Agent", 0, 0L, 0L, 0L, false, "Failed to retrieve results: " + e.getMessage()),
                            new AgentMetric("Validation Agent", 0, 0L, 0L, 0L, false, "Parent execution failed")
                    ),
                    LocalDateTime.now()
            );
            aiMetricsTracker.registerMetric(errorMetric);
            throw new RuntimeException("Failed to retrieve reports from parallel agents", e);
        }

        String atsReport = atsRes.response().content() != null ? atsRes.response().content() : "";
        String recruiterReport = recruiterRes.response().content() != null ? recruiterRes.response().content() : "";
        String keywordReport = keywordRes.response().content() != null ? keywordRes.response().content() : "";

        // 2. Feed intermediate reports to the Validation Agent sequentially
        long validationStart = System.currentTimeMillis();
        AgentResponse validationRes = validationAgent.validateAndConsolidate(
                finalSanitizedText,
                atsReport,
                recruiterReport,
                keywordReport
        );
        long validationLatency = System.currentTimeMillis() - validationStart;

        AgentMetric validationMetric = new AgentMetric(
                "Validation Agent",
                validationLatency,
                validationRes.promptTokens(),
                validationRes.generationTokens(),
                validationRes.totalTokens(),
                validationRes.success(),
                validationRes.errorMessage()
        );

        String rawResponse = validationRes.content() != null ? validationRes.content() : "";

        // 3. Map final JSON string via mapper
        CvAnalysisResponse response = null;
        boolean parseSuccess = false;
        try {
            response = cvAnalysisMapper.toCvAnalysisResponse(rawResponse);
            parseSuccess = true;
        } catch (Exception e) {
            // Keep parsing errors safely logged PII-free
            System.err.println("Failed to parse consolidated CV Analysis JSON: " + e.getMessage());
        }

        long totalLatency = System.currentTimeMillis() - startTime;
        boolean overallSuccess = atsRes.metric().success() && recruiterRes.metric().success() &&
                keywordRes.metric().success() && validationMetric.success() && parseSuccess;

        // 4. Compile and Register WorkflowMetric
        List<AgentMetric> agentMetrics = List.of(
                atsRes.metric(),
                recruiterRes.metric(),
                keywordRes.metric(),
                validationMetric
        );

        WorkflowMetric workflowMetric = new WorkflowMetric(
                correlationId,
                totalLatency,
                overallSuccess,
                response != null ? response.atsScore() : null,
                0, // validationRejectCount
                agentMetrics,
                LocalDateTime.now()
        );

        aiMetricsTracker.registerMetric(workflowMetric);

        if (!overallSuccess && response == null) {
            throw new RuntimeException("AI Workflow execution failed during consolidated analysis parsing.");
        }

        return new WorkflowResult(response, rawResponse);
    }

    @PreDestroy
    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
