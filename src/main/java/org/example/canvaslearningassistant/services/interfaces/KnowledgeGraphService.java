package org.example.canvaslearningassistant.services.interfaces;

import org.example.canvaslearningassistant.entity.GraphEdge;
import org.example.canvaslearningassistant.exception.QuizGenerationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface KnowledgeGraphService {
    List<GraphEdge> getGraph(Integer courseId, String accessToken);

    void updateStudentKnowledge(List<GraphEdge> graphEdges, Integer courseId, Integer studentId, String accessToken) throws QuizGenerationException;
}
