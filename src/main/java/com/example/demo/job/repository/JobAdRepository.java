package com.example.demo.job.repository;

import com.example.demo.job.model.JobAd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JobAdRepository extends JpaRepository<JobAd, UUID> {
}
