package com.example.demo.ai.rag.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
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
