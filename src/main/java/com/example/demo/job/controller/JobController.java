package com.example.demo.job.controller;

import com.example.demo.job.dto.JobMatchRequest;
import com.example.demo.job.dto.JobMatchResponseDto;
import com.example.demo.job.service.JobMatchingService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/jobs")
public class JobController {

    private final JobMatchingService jobMatchingService;

    public JobController(JobMatchingService jobMatchingService) {
        this.jobMatchingService = jobMatchingService;
    }

    @PostMapping("/match")
    public ResponseEntity<JobMatchResponseDto> matchJob(@Valid @RequestBody JobMatchRequest request) {
        JobMatchResponseDto response = jobMatchingService.matchJobAd(request.documentId(), request.jobAdText());
        return ResponseEntity.ok(response);
    }
}
