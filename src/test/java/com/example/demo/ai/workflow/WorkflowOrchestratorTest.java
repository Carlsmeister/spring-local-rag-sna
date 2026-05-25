package com.example.demo.ai.workflow;

import com.example.demo.ai.workflow.metrics.AgentResponse;
import com.example.demo.ai.workflow.metrics.AiMetricsTracker;
import com.example.demo.dto.CvAnalysisResponse;
import com.example.demo.mapper.CvAnalysisMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowOrchestratorTest {

    @Mock
    private AtsAgent atsAgent;

    @Mock
    private RecruiterAgent recruiterAgent;

    @Mock
    private KeywordAgent keywordAgent;

    @Mock
    private ValidationAgent validationAgent;

    @Mock
    private CvAnalysisMapper cvAnalysisMapper;

    @Mock
    private AiMetricsTracker aiMetricsTracker;

    private WorkflowOrchestrator workflowOrchestrator;

    @BeforeEach
    void setUp() {
        workflowOrchestrator = new WorkflowOrchestrator(
                atsAgent,
                recruiterAgent,
                keywordAgent,
                validationAgent,
                cvAnalysisMapper,
                aiMetricsTracker
        );
    }

    @Test
    void testOrchestrate_Success() {
        String rawText = "Sanitized CV raw text";

        when(atsAgent.analyze(rawText)).thenReturn(AgentResponse.success("ATS Review Content", 100L, 50L, 150L));
        when(recruiterAgent.analyze(rawText)).thenReturn(AgentResponse.success("Recruiter Review Content", 120L, 60L, 180L));
        when(keywordAgent.analyze(rawText)).thenReturn(AgentResponse.success("Keyword Review Content", 110L, 55L, 165L));

        when(validationAgent.validateAndConsolidate(
                eq(rawText),
                eq("ATS Review Content"),
                eq("Recruiter Review Content"),
                eq("Keyword Review Content")
        )).thenReturn(AgentResponse.success("final-json-string", 300L, 150L, 450L));

        CvAnalysisResponse expectedResponse = new CvAnalysisResponse(
                90,
                List.of("Strength"),
                List.of("Weakness"),
                List.of("Recommendation")
        );
        when(cvAnalysisMapper.toCvAnalysisResponse("final-json-string")).thenReturn(expectedResponse);

        WorkflowOrchestrator.WorkflowResult result = workflowOrchestrator.orchestrate(rawText);

        assertNotNull(result);
        assertEquals(expectedResponse, result.response());
        assertEquals("final-json-string", result.rawResponse());

        verify(atsAgent).analyze(rawText);
        verify(recruiterAgent).analyze(rawText);
        verify(keywordAgent).analyze(rawText);
        verify(validationAgent).validateAndConsolidate(
                rawText, "ATS Review Content", "Recruiter Review Content", "Keyword Review Content");
        verify(cvAnalysisMapper).toCvAnalysisResponse("final-json-string");
        verify(aiMetricsTracker).registerMetric(any());
    }
}
