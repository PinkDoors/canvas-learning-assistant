package org.example.canvaslearningassistant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AskWithGraphRequest {
    private String userPrompt;
    private Map<String, Integer> knowledge;
    private List<GraphEdgeDto> graphEdges;
}