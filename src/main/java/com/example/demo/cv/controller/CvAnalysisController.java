package com.example.demo.cv.controller;

import com.example.demo.cv.dto.CvAnalysisRequest;
import com.example.demo.cv.dto.CvAnalysisResponseDto;
import com.example.demo.cv.service.CvAnalysisService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cv")
public class CvAnalysisController {

    private final CvAnalysisService cvAnalysisService;

    public CvAnalysisController(CvAnalysisService cvAnalysisService) {
        this.cvAnalysisService = cvAnalysisService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<CvAnalysisResponseDto> analyzeCv(@Valid @RequestBody CvAnalysisRequest request) {
        CvAnalysisResponseDto responseDto = cvAnalysisService.analyzeCv(request.documentId());
        return ResponseEntity.ok(responseDto);
    }
}
