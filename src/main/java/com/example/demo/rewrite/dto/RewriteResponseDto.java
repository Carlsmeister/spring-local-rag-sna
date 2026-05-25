package com.example.demo.rewrite.dto;

import java.util.List;

public record RewriteResponseDto(
    String originalText,
    List<SuggestionDto> suggestions
) {
    public record SuggestionDto(
        String style,
        String text
    ) {}
}
