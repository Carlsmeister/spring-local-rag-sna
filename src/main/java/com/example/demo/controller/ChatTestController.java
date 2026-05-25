package com.example.demo.controller;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class ChatTestController {

    private final ChatModel chatModel;

    public ChatTestController(ChatModel chatModel) {
        this.chatModel = chatModel;
    }

    @GetMapping("/chat")
    public java.util.Map<String, String> chat(@RequestParam String message) {
        return java.util.Map.of("response", chatModel.call(message));
    }
}
