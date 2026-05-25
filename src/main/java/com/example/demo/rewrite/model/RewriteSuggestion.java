package com.example.demo.rewrite.model;

import com.example.demo.model.Document;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rewrite_suggestions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RewriteSuggestion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @Lob
    @Column(name = "original_sentence", columnDefinition = "TEXT", nullable = false)
    private String originalSentence;

    @Enumerated(EnumType.STRING)
    @Column(name = "suggested_style", nullable = false)
    private RewriteStyle suggestedStyle;

    @Lob
    @Column(name = "suggested_text", columnDefinition = "TEXT", nullable = false)
    private String suggestedText;

    @Column(name = "created_date", nullable = false)
    private LocalDateTime createdDate;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
