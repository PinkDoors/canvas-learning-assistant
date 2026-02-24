package org.example.canvaslearningassistant.services;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class LlmService {

    private static final String OLLAMA_URL = "http://localhost:11434/api/chat";
    private static final String MODEL = "qwen3-coder:480b-cloud";

    private final RestTemplate restTemplate = new RestTemplate();

    public String ask(String userMessage) {

        Map<String, Object> requestBody = Map.of(
                "model", MODEL,
                "messages", List.of(
                        Map.of(
                                "role", "user",
                                "content", userMessage
                        )
                ),
                "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(OLLAMA_URL, entity, Map.class);

        Map body = response.getBody();
        Map message = (Map) body.get("message");

        return message.get("content").toString();
    }
}

