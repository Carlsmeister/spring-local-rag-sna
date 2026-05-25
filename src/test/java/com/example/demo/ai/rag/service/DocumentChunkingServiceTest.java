package com.example.demo.ai.rag.service;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class DocumentChunkingServiceTest {

    private final DocumentChunkingService chunkingService = new DocumentChunkingService();

    @Test
    void testChunkText_EmptyOrNull() {
        assertTrue(chunkingService.chunkText(null).isEmpty());
        assertTrue(chunkingService.chunkText("   ").isEmpty());
    }

    @Test
    void testChunkText_SingleParagraphUnderLimit() {
        String text = "This is a single paragraph. It is well under 1000 characters.";
        List<String> chunks = chunkingService.chunkText(text);
        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0));
    }

    @Test
    void testChunkText_MultipleParagraphsUnderLimit() {
        String text = "Paragraph 1.\n\nParagraph 2.\n\nParagraph 3.";
        List<String> chunks = chunkingService.chunkText(text);
        assertEquals(1, chunks.size());
        assertEquals(text, chunks.get(0));
    }

    @Test
    void testChunkText_ParagraphExceedsLimitIsKeptSingle() {
        // Create a single paragraph with 1200 characters
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 120; i++) {
            sb.append("1234567890");
        }
        String longParagraph = sb.toString();
        List<String> chunks = chunkingService.chunkText(longParagraph);
        assertEquals(1, chunks.size());
        assertEquals(longParagraph, chunks.get(0));
    }

    @Test
    void testChunkText_SlidingOverlapCorrectness() {
        // Let's create paragraphs:
        // P1: 600 characters
        // P2: 150 characters
        // P3: 150 characters
        // P4: 600 characters
        // Max chunk size: 1000, Overlap: 200.
        // Chunk 1 should group P1, P2, P3: total length 600 + 2 + 150 + 2 + 150 = 904. (adding P4 would exceed 1000).
        // For Chunk 2, look back from P3 (index 2):
        // P3 (150 chars) <= 200, so included.
        // P2 (150 + 2 = 152 chars). P3 + P2 = 302 > 200, so P2 is not included.
        // So Chunk 2 should start at P3, grouping P3 and P4: total length 150 + 2 + 600 = 752.
        
        String p1 = "a".repeat(600);
        String p2 = "b".repeat(150);
        String p3 = "c".repeat(150);
        String p4 = "d".repeat(600);
        
        String text = p1 + "\n\n" + p2 + "\n\n" + p3 + "\n\n" + p4;
        List<String> chunks = chunkingService.chunkText(text);
        
        assertEquals(2, chunks.size());
        assertEquals(p1 + "\n\n" + p2 + "\n\n" + p3, chunks.get(0));
        assertEquals(p3 + "\n\n" + p4, chunks.get(1));
    }
}
