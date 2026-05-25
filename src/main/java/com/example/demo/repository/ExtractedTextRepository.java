package com.example.demo.repository;

import com.example.demo.model.ExtractedText;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ExtractedTextRepository extends JpaRepository<ExtractedText, UUID> {
}
