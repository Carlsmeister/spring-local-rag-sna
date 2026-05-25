package com.example.demo.job.service;

import com.example.demo.job.dto.JobMatchResponseDto;
import com.example.demo.job.model.JobAd;
import com.example.demo.job.model.JobMatchResult;
import com.example.demo.job.repository.JobAdRepository;
import com.example.demo.job.repository.JobMatchResultRepository;
import com.example.demo.ai.rag.service.EmbeddingService;
import com.example.demo.model.Document;
import com.example.demo.model.DocumentType;
import com.example.demo.model.UserProfile;
import com.example.demo.repository.DocumentRepository;
import com.example.demo.repository.UserProfileRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class JobMatchingServiceTest {

    @Autowired
    private JobMatchingService jobMatchingService;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private JobAdRepository jobAdRepository;

    @Autowired
    private JobMatchResultRepository jobMatchResultRepository;

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private ChatClient.Builder mockChatClientBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @BeforeEach
    void setUp() {
        jobMatchResultRepository.deleteAll();
        jobAdRepository.deleteAll();
        documentRepository.deleteAll();
        userProfileRepository.deleteAll();
    }

    @Test
    void testMatchJobAd_Success() throws Exception {
        // 1. Create and save a UserProfile
        UserProfile userProfile = UserProfile.builder()
                .email("applicant@example.com")
                .firstName("Carl")
                .lastName("Lundholm")
                .createdAt(LocalDateTime.now())
                .build();
        userProfile = userProfileRepository.save(userProfile);

        // 2. Create and save a CV Document
        Document document = Document.builder()
                .fileName("cv_carl.pdf")
                .fileSize(500L)
                .contentType("application/pdf")
                .documentType(DocumentType.CV)
                .userProfile(userProfile)
                .createdAt(LocalDateTime.now())
                .build();
        document = documentRepository.save(document);

        // 3. Index some text chunks for this document
        embeddingService.indexDocument(document.getId(), "Carl is a Spring Boot developer with Docker and Postgres experience.");

        // 4. Mock the local LLM response
        ChatClient chatClient = mockChatClientBuilder.build();
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();
        ChatClient.CallResponseSpec responseSpec = requestSpec.call();

        String rawAiJson = """
            ```json
            {
                "matchPercentage": 85,
                "missingKeywords": ["Kubernetes", "AWS"],
                "matchingStrength": "Very strong backend developer matching.",
                "actionableSuggestions": [
                    "Mention AWS deployment details if applicable.",
                    "Highlight any Kubernetes knowledge."
                ]
            }
            ```
            """;
        when(responseSpec.content()).thenReturn(rawAiJson);

        // 5. Invoke the Service
        String jobAdText = "Seeking a developer who knows Spring Boot, Docker, and Kubernetes for AWS deployments.";
        JobMatchResponseDto result = jobMatchingService.matchJobAd(document.getId(), jobAdText);

        // 6. Assertions on the response DTO
        assertNotNull(result);
        assertEquals(85, result.matchPercentage());
        assertEquals(2, result.missingKeywords().size());
        assertTrue(result.missingKeywords().contains("Kubernetes"));
        assertEquals("Very strong backend developer matching.", result.matchingStrength());
        assertEquals(2, result.actionableSuggestions().size());

        // 7. Verify Database Persistence
        List<JobAd> jobAds = jobAdRepository.findAll();
        assertEquals(1, jobAds.size());
        JobAd savedJobAd = jobAds.get(0);
        assertEquals("Seeking a developer who knows Spring Boot, Docker, and Kubernetes for AWS deployments.", savedJobAd.getRawText());
        assertNotNull(savedJobAd.getTitle());

        List<JobMatchResult> matchResults = jobMatchResultRepository.findAll();
        assertEquals(1, matchResults.size());
        JobMatchResult savedResult = matchResults.get(0);
        assertEquals(85, savedResult.getMatchScore());
        assertEquals(rawAiJson, savedResult.getRawAnalysis());
        assertEquals(document.getId(), savedResult.getDocument().getId());
        assertEquals(savedJobAd.getId(), savedResult.getJobAd().getId());

        // Verify JSON string list mapping
        List<String> persistedMissingKeywords = objectMapper.readValue(
                savedResult.getMissingKeywords(),
                new TypeReference<List<String>>() {}
        );
        assertEquals(2, persistedMissingKeywords.size());
        assertTrue(persistedMissingKeywords.contains("AWS"));
    }

    @Test
    void testMatchJobAd_DocumentNotFound_ThrowsNotFoundException() {
        UUID randomId = UUID.randomUUID();
        assertThrows(ResponseStatusException.class, () -> jobMatchingService.matchJobAd(randomId, "Some job text"));
    }

    @Test
    void testMatchJobAd_NullInputs_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> jobMatchingService.matchJobAd(null, "Some job text"));
        assertThrows(IllegalArgumentException.class, () -> jobMatchingService.matchJobAd(UUID.randomUUID(), null));
        assertThrows(IllegalArgumentException.class, () -> jobMatchingService.matchJobAd(UUID.randomUUID(), "   "));
    }
}
