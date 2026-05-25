package com.example.demo.rewrite.dto;

import java.util.List;

public record RewriteAiResponse(
    String originalText,
    List<AiSuggestion> suggestions
) {
    public record AiSuggestion(
        String style,
        String text
    ) {}
}
