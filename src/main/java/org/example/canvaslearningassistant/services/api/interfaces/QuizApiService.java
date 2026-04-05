package org.example.canvaslearningassistant.services.api.interfaces;

import org.example.canvaslearningassistant.entity.GraphEdge;
import org.example.canvaslearningassistant.model.NewQuizQuestionData;
import org.example.canvaslearningassistant.exceptions.QuizGenerationException;

import java.util.List;

public interface QuizApiService {
    List<NewQuizQuestionData> generateQuizQuestions(List<GraphEdge> graph, int numQuestions) throws QuizGenerationException;
}
