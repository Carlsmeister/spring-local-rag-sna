package com.example.demo.ai.rag.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentChunkingService {

    private static final int MAX_CHUNK_SIZE = 1000;
    private static final int OVERLAP_SIZE = 200;

    /**
     * Chunks the raw text by splitting it by double newlines, and then
     * grouping paragraphs into chunks of up to 1000 characters with a sliding
     * overlap of up to 200 characters.
     *
     * @param rawText the raw text to chunk
     * @return a list of text chunks
     */
    public List<String> chunkText(String rawText) {
        if (rawText == null || rawText.strip().isEmpty()) {
            return List.of();
        }

        // Split by double newlines
        List<String> paragraphs = Arrays.stream(rawText.split("\\n\\n"))
                .map(String::strip)
                .filter(p -> !p.isEmpty())
                .collect(Collectors.toList());

        List<String> chunks = new ArrayList<>();
        int n = paragraphs.size();
        int start = 0;

        while (start < n) {
            List<String> currentParagraphs = new ArrayList<>();
            int currentLength = 0;
            int end = start;

            while (end < n) {
                String p = paragraphs.get(end);
                int additionalLength = p.length();
                if (!currentParagraphs.isEmpty()) {
                    additionalLength += 2; // for "\n\n"
                }

                // If adding this paragraph exceeds the max chunk size, and we already have at least one paragraph, break.
                if (currentLength + additionalLength > MAX_CHUNK_SIZE && !currentParagraphs.isEmpty()) {
                    break;
                }

                currentParagraphs.add(p);
                currentLength += additionalLength;
                end++;
            }

            // Create the chunk
            String chunkText = String.join("\n\n", currentParagraphs);
            chunks.add(chunkText);

            // If we have indexed all paragraphs, we are done
            if (end >= n) {
                break;
            }

            // Determine the overlap start index for the next chunk.
            // Look back from 'end - 1' (the last paragraph in the current chunk) to 'start'.
            int nextStart = end;
            int overlapLength = 0;
            for (int i = end - 1; i > start; i--) {
                String p = paragraphs.get(i);
                int pLen = p.length();
                if (overlapLength > 0) {
                    pLen += 2; // for "\n\n"
                }

                if (overlapLength + pLen <= OVERLAP_SIZE) {
                    overlapLength += pLen;
                    nextStart = i;
                } else {
                    break;
                }
            }

            // Always guarantee forward progress
            if (nextStart <= start) {
                start = start + 1;
            } else {
                start = nextStart;
            }
        }

        return chunks;
    }
}
