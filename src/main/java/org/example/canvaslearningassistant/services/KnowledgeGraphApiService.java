package org.example.canvaslearningassistant.services;

import org.example.canvaslearningassistant.dto.GraphEdgeDto;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class KnowledgeGraphApiService {

    // FastAPI endpoint inside Docker
    private static final String API_URL = "http://localhost:8000/generate_graph";

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Sends user text to the FastAPI knowledge graph API and returns JSON response as String
     */
    public List<GraphEdgeDto> ask(String userMessage) {

        // Build request body
        Map<String, Object> requestBody = Map.of(
                "text", userMessage
        );

        // Set headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // POST request
        ResponseEntity<List<GraphEdgeDto>> response =
                restTemplate.exchange(
                        API_URL,
                        HttpMethod.POST,
                        entity,
                        new ParameterizedTypeReference<>() {
                        }
                );

        return response.getBody();
    }
}