package com.example.demo.rewrite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record RewriteRequest(
    @NotNull(message = "documentId must not be null")
    UUID documentId,

    @NotBlank(message = "originalText must not be blank")
    String originalText,

    List<String> keywords
) {}
