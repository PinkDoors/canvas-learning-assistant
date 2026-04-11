package org.example.canvaslearningassistant.dto;

import org.example.canvaslearningassistant.entity.GraphEdge;

import java.util.List;

public record GraphSessionData(List<GraphEdge> graph, Integer courseId, Integer userId) {
}
