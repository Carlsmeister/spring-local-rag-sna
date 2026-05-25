package com.example.demo.job.repository;

import com.example.demo.job.model.JobMatchResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobMatchResultRepository extends JpaRepository<JobMatchResult, UUID> {
}
