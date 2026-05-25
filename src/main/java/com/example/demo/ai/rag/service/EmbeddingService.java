package com.example.demo.ai.rag.service;

import com.example.demo.ai.rag.service.DocumentChunkingService;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class EmbeddingService {

    private final VectorStore vectorStore;
    private final DocumentChunkingService chunkingService;
    private final JdbcTemplate jdbcTemplate;

    public EmbeddingService(VectorStore vectorStore, DocumentChunkingService chunkingService, JdbcTemplate jdbcTemplate) {
        this.vectorStore = vectorStore;
        this.chunkingService = chunkingService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Chunks the raw text of the document and indexes it in the vector store.
     *
     * @param documentId the ID of the document
     * @param rawText the raw text of the document
     */
    public void indexDocument(UUID documentId, String rawText) {
        List<String> chunks = chunkingService.chunkText(rawText);
        List<Document> documents = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", documentId.toString());
            metadata.put("chunkIndex", i);

            Document doc = new Document(chunks.get(i), metadata);
            documents.add(doc);
        }

        if (!documents.isEmpty()) {
            vectorStore.add(documents);
        }
    }

    /**
     * Performs similarity search for the given query, filtering by the specific documentId.
     *
     * @param documentId the ID of the document to filter by
     * @param query the query to search for
     * @param topK the number of top results to return
     * @return the list of matched documents
     */
    public List<Document> searchChunks(UUID documentId, String query, int topK) {
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression("documentId == '" + documentId.toString() + "'")
                .build();
        return vectorStore.similaritySearch(searchRequest);
    }

    /**
     * Deletes all indexed chunks for the given documentId from the PGVector store.
     *
     * @param documentId the ID of the document
     */
    public void deleteChunks(UUID documentId) {
        jdbcTemplate.update("DELETE FROM vector_store WHERE metadata->>'documentId' = ?", documentId.toString());
    }
}
