package com.example.demo.ai.rag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class EmbeddingServiceTest {

    @Autowired
    private EmbeddingService embeddingService;

    @Autowired
    private VectorStore vectorStore;

    @MockitoBean
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private ChatClient.Builder chatClientBuilder;

    @BeforeEach
    void setUp() {
        ChatClient chatClient = mock(ChatClient.class);
        ChatClient.ChatClientRequestSpec requestSpec = mock(ChatClient.ChatClientRequestSpec.class);
        ChatClient.CallResponseSpec responseSpec = mock(ChatClient.CallResponseSpec.class);

        when(chatClientBuilder.build()).thenReturn(chatClient);
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(responseSpec);
        when(responseSpec.content()).thenReturn("Java React backend Spring");
    }

    @Test
    void testIndexAndSearchChunks() {
        UUID docId1 = UUID.randomUUID();
        UUID docId2 = UUID.randomUUID();

        String rawText1 = "This is the first paragraph of document one.\n\nThis is the second paragraph.";
        String rawText2 = "This is a completely different document with some text.";

        // Index both
        embeddingService.indexDocument(docId1, rawText1);
        embeddingService.indexDocument(docId2, rawText2);

        // Search for docId1 chunks
        List<Document> results1 = embeddingService.searchChunks(docId1, "paragraph", 5);
        assertNotNull(results1);
        assertFalse(results1.isEmpty());

        // Verify all returned results belong to docId1
        for (Document doc : results1) {
            assertEquals(docId1.toString(), doc.getMetadata().get("documentId"));
            assertNotNull(doc.getMetadata().get("chunkIndex"));
        }

        // Search for docId2 chunks
        List<Document> results2 = embeddingService.searchChunks(docId2, "different", 5);
        assertNotNull(results2);
        assertFalse(results2.isEmpty());

        for (Document doc : results2) {
            assertEquals(docId2.toString(), doc.getMetadata().get("documentId"));
        }
    }

    @Test
    void testSearchChunks_HybridSearch_RrfMerging() {
        UUID docId = UUID.randomUUID();
        String rawText = "Carl Lundholm is a System Developer student. Experienced in Java and React.";

        // Index in the real vectorStore (will write to SimpleVectorStore)
        embeddingService.indexDocument(docId, rawText);

        // Mock the jdbcTemplate to return a keyword match (simulating a DB hit for FTS)
        Document kwDoc = new Document("chunk-id-2", "Carl Lundholm has experience in Kotlin and PostgreSQL.", Map.of("documentId", docId.toString()));
        when(jdbcTemplate.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), any(), any(), any(), any()))
                .thenReturn(List.of(kwDoc));

        // Call searchChunks (should trigger both vectorStore and jdbcTemplate, then merge them)
        List<Document> results = embeddingService.searchChunks(docId, "Java Kotlin", 5);

        assertNotNull(results);
        assertFalse(results.isEmpty());

        // Verify both the semantic result (from SimpleVectorStore) and the keyword result (from mock SQL) are present
        boolean hasSemantic = false;
        boolean hasKeyword = false;
        for (Document doc : results) {
            if (doc.getText().contains("Java and React")) {
                hasSemantic = true;
            }
            if (doc.getText().contains("Kotlin and PostgreSQL")) {
                hasKeyword = true;
            }
        }

        assertTrue(hasSemantic, "Should contain the semantic match from SimpleVectorStore");
        assertTrue(hasKeyword, "Should contain the keyword match from database");
    }

    @Test
    void testExpandQuery() {
        String query = "Frontend developer";
        String expanded = embeddingService.expandQuery(query);
        assertNotNull(expanded);
        assertTrue(expanded.contains("Frontend developer"));
        assertTrue(expanded.contains("Java React backend Spring"));
    }

    @Test
    void testSearchChunks_HierarchicalMetadataFiltering() {
        UUID docId = UUID.randomUUID();

        // Pad the experience text to be longer than 1000 characters, forcing a chunk split
        String paddedExperience = "Worked as a backend developer for 5 years. ".repeat(30);
        String rawText = "# Experience\n" + paddedExperience + "\n\n# Skills\nJava, Spring Boot, React, and PostgreSQL.";

        // Index the document (will write to SimpleVectorStore)
        embeddingService.indexDocument(docId, rawText);

        // Search chunks filtering specifically for the "Skills" section
        List<Document> skillsResults = embeddingService.searchChunks(docId, "Java", "Skills", 5);
        assertNotNull(skillsResults);
        assertFalse(skillsResults.isEmpty());

        for (Document doc : skillsResults) {
            assertEquals("Skills", doc.getMetadata().get("section"));
            assertTrue(doc.getText().contains("Java"));
        }

        // Search chunks filtering specifically for the "Experience" section
        List<Document> experienceResults = embeddingService.searchChunks(docId, "developer", "Experience", 5);
        assertNotNull(experienceResults);
        assertFalse(experienceResults.isEmpty());

        for (Document doc : experienceResults) {
            assertEquals("Experience", doc.getMetadata().get("section"));
            assertTrue(doc.getText().contains("developer"));
        }
    }

    @Test
    void testDeleteChunks() {
        UUID docId = UUID.randomUUID();

        // Call delete chunks
        embeddingService.deleteChunks(docId);

        // Verify jdbcTemplate update was called with correct SQL and parameter
        verify(jdbcTemplate, times(1)).update(
                eq("DELETE FROM vector_store WHERE metadata->>'documentId' = ?"),
                eq(docId.toString())
        );
    }
}
