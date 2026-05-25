package com.example.demo.job.model;

import com.example.demo.model.Document;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_match_results")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobMatchResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_ad_id", nullable = false)
    private JobAd jobAd;

    @Column(name = "match_score", nullable = false)
    private Integer matchScore;

    @Lob
    @Column(name = "missing_keywords", columnDefinition = "TEXT")
    private String missingKeywords;

    @Lob
    @Column(name = "raw_analysis", columnDefinition = "TEXT", nullable = false)
    private String rawAnalysis;

    @Column(name = "matched_at", nullable = false)
    private LocalDateTime matchedAt;

    @PrePersist
    protected void onCreate() {
        matchedAt = LocalDateTime.now();
    }
}
