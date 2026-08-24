package com.example.demo.ai.rag.service;

import com.example.demo.ai.rag.service.DocumentChunkingService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
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

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    private final VectorStore vectorStore;
    private final DocumentChunkingService chunkingService;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ChatClient.Builder chatClientBuilder;

    public EmbeddingService(
            VectorStore vectorStore,
            DocumentChunkingService chunkingService,
            JdbcTemplate jdbcTemplate,
            ChatClient.Builder chatClientBuilder) {
        this.vectorStore = vectorStore;
        this.chunkingService = chunkingService;
        this.jdbcTemplate = jdbcTemplate;
        this.chatClientBuilder = chatClientBuilder;
        this.objectMapper = new ObjectMapper();
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

        String fileName = "CV";
        try {
            fileName = jdbcTemplate.queryForObject(
                    "SELECT file_name FROM documents WHERE id = ?",
                    String.class,
                    documentId
            );
            if (fileName == null) {
                fileName = "CV";
            }
        } catch (Exception e) {
            // Fallback for mocked jdbcTemplate in tests or database errors
            fileName = "CV";
        }

        String currentSection = "General";
        for (int i = 0; i < chunks.size(); i++) {
            String chunkContent = chunks.get(i);
            currentSection = detectSection(chunkContent, currentSection);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("documentId", documentId.toString());
            metadata.put("chunkIndex", i);
            metadata.put("section", currentSection);

            // Prepend contextual information for Contextual Retrieval
            String contextualHeader = String.format("[Document Context: %s | Section Index: %d]\n", fileName, i + 1);
            String contentWithContext = contextualHeader + chunks.get(i);

            Document doc = new Document(contentWithContext, metadata);
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
    /**
     * Performs similarity search for the given query using a hybrid approach.
     * Combines semantic vector similarity search with sparse database keyword search
     * (PostgreSQL Full-Text Search or fallback LIKE query) using Reciprocal Rank Fusion (RRF).
     *
     * @param documentId the ID of the document to filter by
     * @param query the query to search for
     * @param topK the number of top results to return
     * @return the list of matched documents
     */
    public List<Document> searchChunks(UUID documentId, String query, int topK) {
        return searchChunks(documentId, query, null, topK);
    }

    /**
     * Performs similarity search for the given query using a hybrid approach,
     * filtering by both documentId and section category (if specified).
     *
     * @param documentId the ID of the document to filter by
     * @param query the query to search for
     * @param section the section category to filter by (e.g. "Experience", "Skills", "Education") or null
     * @param topK the number of top results to return
     * @return the list of matched documents
     */
    public List<Document> searchChunks(UUID documentId, String query, String section, int topK) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        // Pre-process and expand query using local LLM
        String expandedQuery = expandQuery(query);

        // 1. Fetch Semantic Results via Vector Similarity Search (fetching extra for RRF)
        int fetchLimit = topK * 2;
        String filterExpression = "documentId == '" + documentId.toString() + "'";
        if (section != null && !section.isBlank()) {
            filterExpression += " && section == '" + section + "'";
        }

        SearchRequest searchRequest = SearchRequest.builder()
                .query(expandedQuery)
                .topK(fetchLimit)
                .filterExpression(filterExpression)
                .build();
        
        List<Document> semanticResults = List.of();
        try {
            semanticResults = vectorStore.similaritySearch(searchRequest);
        } catch (Exception e) {
            log.warn("Vector similarity search failed: {}", e.getMessage());
        }

        // 2. Fetch Keyword Results via SQL Full-Text Search
        List<Document> keywordResults = new ArrayList<>();
        try {
            // Try Postgres Full-Text Search
            keywordResults = queryPostgresFts(documentId, expandedQuery, section, fetchLimit);
        } catch (Exception e) {
            log.debug("PostgreSQL Full-Text Search failed or is unsupported (e.g. H2 database). Falling back to SQL LIKE search. Error: {}", e.getMessage());
            try {
                keywordResults = queryLikeSearch(documentId, expandedQuery, section, fetchLimit);
            } catch (Exception ex) {
                log.debug("Fallback LIKE keyword search failed: {}", ex.getMessage());
            }
        }

        // 3. Merge Results using Reciprocal Rank Fusion (RRF)
        return reciprocalRankFusion(semanticResults, keywordResults, topK);
    }

    private String detectSection(String chunkContent, String lastSection) {
        String textUpper = chunkContent.toUpperCase();
        if (textUpper.contains("# SUMMARY") || textUpper.contains("## SUMMARY")) {
            return "Summary";
        } else if (textUpper.contains("# EXPERIENCE") || textUpper.contains("## EXPERIENCE") || textUpper.contains("# WORK EXPERIENCE") || textUpper.contains("## WORK EXPERIENCE")) {
            return "Experience";
        } else if (textUpper.contains("# EDUCATION") || textUpper.contains("## EDUCATION")) {
            return "Education";
        } else if (textUpper.contains("# SKILLS") || textUpper.contains("## SKILLS") || textUpper.contains("# TECHNICAL SKILLS") || textUpper.contains("## TECHNICAL SKILLS")) {
            return "Skills";
        } else if (textUpper.contains("# PROJECTS") || textUpper.contains("## PROJECTS")) {
            return "Projects";
        } else if (textUpper.contains("# LANGUAGES") || textUpper.contains("## LANGUAGES")) {
            return "Languages";
        } else if (textUpper.contains("# CONTACT") || textUpper.contains("## CONTACT")) {
            return "Contacts";
        }
        return lastSection;
    }

    /**
     * Expands the query with technical synonyms and related terms using a local LLM pass.
     *
     * @param originalQuery the search query to expand
     * @return the expanded query string
     */
    public String expandQuery(String originalQuery) {
        if (originalQuery == null || originalQuery.isBlank()) {
            return "";
        }

        String systemPrompt = "You are a technical recruiter assistant.\n" +
                "Your task is to take a job search query and expand it with 3-5 relevant alternative keywords, technologies, synonyms, or related terms.\n" +
                "Only output the expanded keywords separated by spaces. Do not write any conversational text, explanations, or quotes.";

        try {
            ChatClient chatClient = chatClientBuilder.build();
            if (chatClient == null) {
                log.warn("ChatClient could not be built from builder; falling back to original query.");
                return originalQuery;
            }
            log.info("Expanding query via local LLM: {}", originalQuery);
            String expanded = chatClient.prompt()
                    .system(systemPrompt)
                    .user("Query to expand: " + originalQuery)
                    .call()
                    .content();

            if (expanded != null && !expanded.isBlank()) {
                String cleanExpanded = expanded.replaceAll("[^a-zA-Z0-9\\s]", " ").trim();
                log.info("Expanded query: '{}' -> '{}'", originalQuery, cleanExpanded);
                return originalQuery + " " + cleanExpanded;
            }
        } catch (Exception e) {
            log.warn("Query expansion failed, falling back to original query. Error: {}", e.getMessage());
        }
        return originalQuery;
    }

    private List<Document> queryPostgresFts(UUID documentId, String query, String section, int limit) {
        boolean hasSection = section != null && !section.isBlank();
        String sql = "SELECT id, content, metadata, " +
                "ts_rank_cd(to_tsvector('english', content), plainto_tsquery('english', ?)) as rank " +
                "FROM vector_store " +
                "WHERE metadata->>'documentId' = ? " +
                (hasSection ? "  AND metadata->>'section' = ? " : "") +
                "  AND to_tsvector('english', content) @@ plainto_tsquery('english', ?) " +
                "ORDER BY rank DESC " +
                "LIMIT ?";

        String queryParam = query.trim();
        if (hasSection) {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                String id = rs.getString("id");
                String content = rs.getString("content");
                String metadataJson = rs.getString("metadata");
                Map<String, Object> metadata = parseMetadataJson(metadataJson);
                return new Document(id, content, metadata);
            }, queryParam, documentId.toString(), section, queryParam, limit);
        } else {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                String id = rs.getString("id");
                String content = rs.getString("content");
                String metadataJson = rs.getString("metadata");
                Map<String, Object> metadata = parseMetadataJson(metadataJson);
                return new Document(id, content, metadata);
            }, queryParam, documentId.toString(), queryParam, limit);
        }
    }

    private List<Document> queryLikeSearch(UUID documentId, String query, String section, int limit) {
        boolean hasSection = section != null && !section.isBlank();
        String[] words = query.toLowerCase().split("\\s+");
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("SELECT id, content, metadata FROM vector_store WHERE metadata->>'documentId' = ? ");
        if (hasSection) {
            queryBuilder.append("AND metadata->>'section' = ? ");
        }
        queryBuilder.append("AND (");

        List<Object> params = new ArrayList<>();
        params.add(documentId.toString());
        if (hasSection) {
            params.add(section);
        }

        int checkCount = 0;
        for (String word : words) {
            String cleanWord = word.trim();
            if (cleanWord.length() < 2) continue; // Skip single characters to avoid noise
            if (checkCount > 0) {
                queryBuilder.append(" OR ");
            }
            queryBuilder.append("LOWER(content) LIKE ?");
            params.add("%" + cleanWord.replaceAll("[%_]", "\\\\$0") + "%");
            checkCount++;
        }

        if (checkCount == 0) {
            return List.of();
        }

        queryBuilder.append(") LIMIT ?");
        params.add(limit);

        return jdbcTemplate.query(queryBuilder.toString(), (rs, rowNum) -> {
            String id = rs.getString("id");
            String content = rs.getString("content");
            String metadataJson = rs.getString("metadata");
            Map<String, Object> metadata = parseMetadataJson(metadataJson);
            return new Document(id, content, metadata);
        }, params.toArray());
    }

    private List<Document> reciprocalRankFusion(List<Document> semanticList, List<Document> keywordList, int topK) {
        Map<String, Double> rrfScores = new HashMap<>();
        Map<String, Document> docRegistry = new HashMap<>();

        int k = 60; // Standard constant for RRF

        // Process semantic list
        for (int rank = 0; rank < semanticList.size(); rank++) {
            Document doc = semanticList.get(rank);
            String docId = doc.getId();
            docRegistry.put(docId, doc);
            rrfScores.put(docId, rrfScores.getOrDefault(docId, 0.0) + (1.0 / (k + rank + 1)));
        }

        // Process keyword list
        for (int rank = 0; rank < keywordList.size(); rank++) {
            Document doc = keywordList.get(rank);
            String docId = doc.getId();
            docRegistry.put(docId, doc);
            rrfScores.put(docId, rrfScores.getOrDefault(docId, 0.0) + (1.0 / (k + rank + 1)));
        }

        // Sort by RRF score descending
        List<String> sortedIds = new ArrayList<>(rrfScores.keySet());
        sortedIds.sort((id1, id2) -> Double.compare(rrfScores.get(id2), rrfScores.get(id1)));

        // Return topK documents
        List<Document> mergedResults = new ArrayList<>();
        int limit = Math.min(topK, sortedIds.size());
        for (int i = 0; i < limit; i++) {
            mergedResults.add(docRegistry.get(sortedIds.get(i)));
        }

        return mergedResults;
    }

    private Map<String, Object> parseMetadataJson(String json) {
        if (json == null || json.isBlank()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse metadata JSON from database: {}", e.getMessage());
            return new HashMap<>();
        }
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
