package org.example.canvaslearningassistant.controllers;

import org.example.canvaslearningassistant.dto.GraphEdgeDto;
import org.example.canvaslearningassistant.services.KnowledgeGraphApiService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final KnowledgeGraphApiService knowledgeGraphApiService;

    public ChatController(KnowledgeGraphApiService knowledgeGraphApiService) {
        this.knowledgeGraphApiService = knowledgeGraphApiService;
    }

    @PostMapping
    public Map<String, String> chat(@RequestBody Map<String, String> body) {
        String userMessage = body.get("message");
        List<GraphEdgeDto> response = knowledgeGraphApiService.ask(userMessage);
        return Map.of("response", response.toString());
    }
}
