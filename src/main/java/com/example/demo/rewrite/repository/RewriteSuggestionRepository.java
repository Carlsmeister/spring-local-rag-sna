package com.example.demo.rewrite.repository;

import com.example.demo.rewrite.model.RewriteSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RewriteSuggestionRepository extends JpaRepository<RewriteSuggestion, UUID> {
}
