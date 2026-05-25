package com.example.demo.job.model;

import com.example.demo.model.UserProfile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "job_ads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobAd {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "company")
    private String company;

    @Lob
    @Column(name = "raw_text", columnDefinition = "TEXT", nullable = false)
    private String rawText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_profile_id")
    private UserProfile userProfile;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
