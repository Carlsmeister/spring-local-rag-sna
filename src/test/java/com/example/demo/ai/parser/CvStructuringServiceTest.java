package com.example.demo.ai.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class CvStructuringServiceTest {

    private CvStructuringService service;
    private ChatClient.Builder mockBuilder;
    private ChatClient mockChatClient;
    private ChatClient.ChatClientRequestSpec mockRequestSpec;
    private ChatClient.CallResponseSpec mockResponseSpec;
    private ChatResponse mockChatResponse;
    private Generation mockGeneration;

    @BeforeEach
    void setUp() {
        mockBuilder = Mockito.mock(ChatClient.Builder.class);
        mockChatClient = Mockito.mock(ChatClient.class);
        mockRequestSpec = Mockito.mock(ChatClient.ChatClientRequestSpec.class);
        mockResponseSpec = Mockito.mock(ChatClient.CallResponseSpec.class);
        mockChatResponse = Mockito.mock(ChatResponse.class);
        mockGeneration = Mockito.mock(Generation.class);

        when(mockBuilder.build()).thenReturn(mockChatClient);
        when(mockChatClient.prompt()).thenReturn(mockRequestSpec);
        when(mockRequestSpec.system(anyString())).thenReturn(mockRequestSpec);
        when(mockRequestSpec.user(anyString())).thenReturn(mockRequestSpec);
        when(mockRequestSpec.call()).thenReturn(mockResponseSpec);
        when(mockResponseSpec.chatResponse()).thenReturn(mockChatResponse);
        when(mockChatResponse.getResult()).thenReturn(mockGeneration);

        service = new CvStructuringService(mockBuilder);

        // Inject the resource via ReflectionTestUtils
        Resource mockResource = new ByteArrayResource("Mock prompt template".getBytes(StandardCharsets.UTF_8));
        ReflectionTestUtils.setField(service, "systemPromptResource", mockResource);
    }

    @Test
    void testStructureCv_Success() {
        String rawText = "Carl Lundholm \n\n experience in Java";
        String structuredOutput = "# Profile\nCarl Lundholm\n\n# Skills\nJava";

        org.springframework.ai.chat.messages.AssistantMessage assistantMessage = Mockito.mock(org.springframework.ai.chat.messages.AssistantMessage.class);
        when(assistantMessage.getText()).thenReturn(structuredOutput);
        when(mockGeneration.getOutput()).thenReturn(assistantMessage);

        String result = service.structureCv(rawText);

        assertEquals(structuredOutput, result);
    }

    @Test
    void testStructureCv_EmptyInput() {
        String result = service.structureCv("");
        assertEquals("", result);

        result = service.structureCv(null);
        assertEquals("", result);
    }

    @Test
    void testStructureCv_LlmFailure_ReturnsRawText() {
        String rawText = "Carl Lundholm \n\n experience in Java";

        // Simulate exception during LLM call
        when(mockRequestSpec.call()).thenThrow(new RuntimeException("LLM down"));

        String result = service.structureCv(rawText);

        // Should return the original raw text instead of crashing
        assertEquals(rawText, result);
    }
}
