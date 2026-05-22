package com.example.demo.controller;

import com.example.demo.dto.CvAnalysisResponse;
import com.example.demo.service.AiService;
import org.apache.tika.Tika;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class ChatController {

    private final ChatClient chatClient;
    private final AiService aiService;
    private final Tika tika;

    public ChatController(ChatClient.Builder builder, AiService aiService) {
        this.chatClient = builder.build();
        this.aiService = aiService;
        this.tika = new Tika();
    }

    @GetMapping("/chat")
    public String chat(@RequestParam String message) {
        return chatClient.prompt(message).call().content();
    }

    @PostMapping("/cv/upload")
    public CvAnalysisResponse upload(@RequestParam("file") MultipartFile file) {
        try {
            // Extract text from the uploaded file (automatically supports PDF, DOCX, TXT, etc.)
            String cvText = tika.parseToString(file.getInputStream());
            if (cvText == null || cvText.isBlank()) {
                throw new IllegalArgumentException("The uploaded file does not contain any readable text");
            }
            return aiService.analyzeCv(cvText);
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to process and analyze the CV: " + e.getMessage(),
                    e
            );
        }
    }
}