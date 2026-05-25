package com.example.demo.cv.repository;

import com.example.demo.cv.model.CvAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CvAnalysisRepository extends JpaRepository<CvAnalysis, UUID> {
}
