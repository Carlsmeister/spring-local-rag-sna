package com.example.demo.service;

import com.example.demo.dto.CvAnalysisResponse;
import com.example.demo.mapper.CvAnalysisMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final CvAnalysisMapper cvAnalysisMapper;

    public AiService(ChatClient.Builder builder, CvAnalysisMapper cvAnalysisMapper) {
        this.chatClient = builder.build();
        this.cvAnalysisMapper = cvAnalysisMapper;
    }

    public CvAnalysisResponse analyzeCv(String cvText) {

        String prompt = """
        Analyze this CV.

        Focus on:
        - professionalism
        - ATS optimization
        - missing keywords
        - readability
        
        Return ONLY valid JSON.
        
        {
          "atsScore": number,
          "strengths": [],
          "weaknesses": [],
          "recommendations": []
        }

        CV:
        """ + cvText;

        String rawResponse = chatClient
                .prompt(prompt)
                .call()
                .content();

        return cvAnalysisMapper.toCvAnalysisResponse(rawResponse);
    }
}