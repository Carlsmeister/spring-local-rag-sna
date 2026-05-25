package com.example.demo.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record UploadResponseDto(
    UUID documentId,
    String fileName,
    long fileSize,
    String contentType,
    String extractedText,
    LocalDateTime parsedAt
) {}

