package com.example.demo.controller;

import com.example.demo.document.service.DocumentService;
import com.example.demo.dto.UploadResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private static final Logger log = LoggerFactory.getLogger(DocumentController.class);
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    /**
     * Accepts a multipart document upload, validates its extension/content,
     * extracts its text using DocumentService, and returns metadata and the extracted text.
     *
     * @param file the uploaded file
     * @return 202 Accepted with metadata and extracted text, or 400 Bad Request
     */

    @PostMapping("/upload")
    public ResponseEntity<UploadResponseDto> uploadDocument(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File must not be empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new org.springframework.web.multipart.MaxUploadSizeExceededException(5 * 1024 * 1024);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || (!originalFilename.toLowerCase().endsWith(".pdf") && !originalFilename.toLowerCase().endsWith(".docx"))) {
            throw new IllegalArgumentException("Invalid file type. Only PDF and DOCX files are allowed.");
        }

        try {
            UploadResponseDto response = documentService.uploadAndParseDocument(
                    originalFilename,
                    file.getSize(),
                    file.getContentType(),
                    file.getInputStream()
            );
            
            // HTTP 202 Accepted as specified by the requirements
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        } catch (IOException e) {
            log.error("Failed to read uploaded file input stream", e);
            throw new RuntimeException("Failed to read file.", e);
        }
    }
}

