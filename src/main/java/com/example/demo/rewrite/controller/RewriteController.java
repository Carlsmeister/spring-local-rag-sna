package com.example.demo.rewrite.controller;

import com.example.demo.ai.validation.FactualValidationException;
import com.example.demo.rewrite.dto.RewriteRequest;
import com.example.demo.rewrite.dto.RewriteResponseDto;
import com.example.demo.rewrite.service.RewriteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/rewrite")
public class RewriteController {

    private final RewriteService rewriteService;

    public RewriteController(RewriteService rewriteService) {
        this.rewriteService = rewriteService;
    }

    @PostMapping("/section")
    public ResponseEntity<RewriteResponseDto> rewriteSection(@Valid @RequestBody RewriteRequest request) {
        RewriteResponseDto response = rewriteService.rewriteSection(
                request.documentId(),
                request.originalText(),
                request.keywords()
        );
        return ResponseEntity.ok(response);
    }
}
