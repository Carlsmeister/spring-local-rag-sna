package com.example.demo.document.service;

import com.example.demo.ai.rag.service.EmbeddingService;
import com.example.demo.document.parser.TikaParserService;
import com.example.demo.dto.UploadResponseDto;
import com.example.demo.model.Document;
import com.example.demo.model.DocumentType;
import com.example.demo.model.ExtractedText;
import com.example.demo.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class DocumentService {

    private final TikaParserService tikaParserService;
    private final DocumentRepository documentRepository;
    private final EmbeddingService embeddingService;

    public DocumentService(TikaParserService tikaParserService, DocumentRepository documentRepository, EmbeddingService embeddingService) {
        this.tikaParserService = tikaParserService;
        this.documentRepository = documentRepository;
        this.embeddingService = embeddingService;
    }

    @Transactional
    public UploadResponseDto uploadAndParseDocument(String fileName, long fileSize, String contentType, InputStream inputStream) throws IOException {
        if (fileSize > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds the maximum limit of 5MB.");
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("File content stream is missing.");
        }

        BufferedInputStream bis = inputStream instanceof BufferedInputStream ? 
                (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);

        // 1. Magic byte MIME type verification
        String detectedMime = tikaParserService.detectMimeType(bis);
        if (!"application/pdf".equals(detectedMime) && 
            !"application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(detectedMime)) {
            throw new IllegalArgumentException("Invalid file type. Only PDF and DOCX files are allowed.");
        }

        // 2. Parse raw text
        String extractedTextContent = tikaParserService.parse(bis);
        
        // 3. Minimum text length constraint (100 characters)
        if (extractedTextContent == null || extractedTextContent.trim().length() < 100) {
            throw new IllegalArgumentException("Extracted text must be at least 100 characters.");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        Document document = Document.builder()
                .fileName(fileName)
                .fileSize(fileSize)
                .contentType(detectedMime) // use the verified magic MIME type
                .documentType(DocumentType.CV) // Default to CV for MVP
                .createdAt(now)
                .build();
        
        ExtractedText extractedText = ExtractedText.builder()
                .document(document)
                .rawContent(extractedTextContent)
                .parsedAt(now)
                .build();
        
        document.setExtractedText(extractedText);
        
        Document savedDocument = documentRepository.save(document);
        
        // Index the document chunks in the vector store
        embeddingService.indexDocument(savedDocument.getId(), extractedTextContent);
        
        return new UploadResponseDto(
                savedDocument.getId(),
                savedDocument.getFileName(),
                savedDocument.getFileSize(),
                savedDocument.getContentType(),
                extractedTextContent,
                now
        );
    }
}
