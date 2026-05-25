package com.example.demo.rewrite.service;

import com.example.demo.ai.validation.FactualValidationException;
import com.example.demo.model.Document;
import com.example.demo.model.DocumentType;
import com.example.demo.model.UserProfile;
import com.example.demo.repository.DocumentRepository;
import com.example.demo.repository.UserProfileRepository;
import com.example.demo.rewrite.dto.RewriteResponseDto;
import com.example.demo.rewrite.model.RewriteStyle;
import com.example.demo.rewrite.model.RewriteSuggestion;
import com.example.demo.rewrite.repository.RewriteSuggestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class RewriteServiceTest {

    @Autowired
    private RewriteService rewriteService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private RewriteSuggestionRepository rewriteSuggestionRepository;

    @Autowired
    private ChatClient.Builder mockChatClientBuilder;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public ChatClient.Builder chatClientBuilder() {
            ChatClient.Builder builder = mock(ChatClient.Builder.class);
            ChatClient chatClient = mock(ChatClient.class);
            ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
            ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

            when(builder.build()).thenReturn(chatClient);
            when(chatClient.prompt()).thenReturn(requestSpec);
            when(requestSpec.system(anyString())).thenReturn(requestSpec);
            when(requestSpec.user(anyString())).thenReturn(requestSpec);
            when(requestSpec.call()).thenReturn(responseSpec);

            return builder;
        }
    }

    private Document document;

    @BeforeEach
    void setUp() {
        rewriteSuggestionRepository.deleteAll();
        documentRepository.deleteAll();
        userProfileRepository.deleteAll();

        // Setup common test data
        UserProfile userProfile = UserProfile.builder()
                .email("test.candidate@example.com")
                .firstName("Jane")
                .lastName("Doe")
                .createdAt(LocalDateTime.now())
                .build();
        userProfile = userProfileRepository.save(userProfile);

        document = Document.builder()
                .fileName("cv_jane.pdf")
                .fileSize(1024L)
                .contentType("application/pdf")
                .documentType(DocumentType.CV)
                .userProfile(userProfile)
                .createdAt(LocalDateTime.now())
                .build();
        document = documentRepository.save(document);
    }

    @Test
    void testRewriteSection_Success() {
        // 1. Mock LLM Response
        ChatClient chatClient = mockChatClientBuilder.build();
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        ChatClient.CallResponseSpec responseSpec = requestSpec.call();

        String rawAiJson = """
            {
                "originalText": "Developed database applications.",
                "suggestions": [
                    {
                        "style": "ACTION_ORIENTED",
                        "text": "Engineered robust and scalable database applications."
                    },
                    {
                        "style": "CONCISE",
                        "text": "Created database systems."
                    }
                ]
            }
            """;
        when(responseSpec.content()).thenReturn(rawAiJson);

        // 2. Invoke Service
        String originalText = "Developed database applications.";
        List<String> keywords = List.of("database", "scalable");
        RewriteResponseDto result = rewriteService.rewriteSection(document.getId(), originalText, keywords);

        // 3. Assertions
        assertNotNull(result);
        assertEquals(originalText, result.originalText());
        assertEquals(2, result.suggestions().size());

        assertEquals("ACTION_ORIENTED", result.suggestions().get(0).style());
        assertEquals("Engineered robust and scalable database applications.", result.suggestions().get(0).text());

        assertEquals("CONCISE", result.suggestions().get(1).style());
        assertEquals("Created database systems.", result.suggestions().get(1).text());

        // 4. Verify Database Persistence
        List<RewriteSuggestion> savedSuggestions = rewriteSuggestionRepository.findAll();
        assertEquals(2, savedSuggestions.size());

        RewriteSuggestion sug1 = savedSuggestions.stream()
                .filter(s -> s.getSuggestedStyle() == RewriteStyle.ACTION_ORIENTED)
                .findFirst()
                .orElse(null);
        assertNotNull(sug1);
        assertEquals(document.getId(), sug1.getDocument().getId());
        assertEquals(originalText, sug1.getOriginalSentence());
        assertEquals("Engineered robust and scalable database applications.", sug1.getSuggestedText());
    }

    @Test
    void testRewriteSection_HallucinationThrowsExceptionAndRollsBack() {
        // 1. Mock LLM Response with a fabricated Proper Noun (AWS)
        ChatClient chatClient = mockChatClientBuilder.build();
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        ChatClient.CallResponseSpec responseSpec = requestSpec.call();

        String rawAiJson = """
            {
                "originalText": "Developed database applications.",
                "suggestions": [
                    {
                        "style": "ACTION_ORIENTED",
                        "text": "Engineered robust database applications in AWS."
                    }
                ]
            }
            """;
        when(responseSpec.content()).thenReturn(rawAiJson);

        // 2. Invoke Service and expect exception
        String originalText = "Developed database applications.";
        List<String> keywords = List.of("database");

        assertThrows(FactualValidationException.class, () ->
                rewriteService.rewriteSection(document.getId(), originalText, keywords));

        // 3. Verify Transaction Rolled Back (No Suggestions saved)
        List<RewriteSuggestion> savedSuggestions = rewriteSuggestionRepository.findAll();
        assertTrue(savedSuggestions.isEmpty(), "No rewrite suggestions should be saved on validation failure");
    }

    @Test
    void testRewriteSection_DocumentNotFound_ThrowsResponseStatusException() {
        UUID randomId = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () ->
                rewriteService.rewriteSection(randomId, "Some text", List.of()));
    }

    @Test
    void testRewriteSection_NullOrBlankInputs_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                rewriteService.rewriteSection(null, "Some text", List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                rewriteService.rewriteSection(document.getId(), null, List.of()));
        assertThrows(IllegalArgumentException.class, () ->
                rewriteService.rewriteSection(document.getId(), "   ", List.of()));
    }
}
