package org.example.canvaslearningassistant.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canvaslearningassistant.entity.GraphEdge;
import org.example.canvaslearningassistant.model.NewQuizQuestionData;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuiz;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestion;
import org.example.canvaslearningassistant.exceptions.QuizGenerationException;
import org.example.canvaslearningassistant.repository.CanvasQuizRepository;
import org.example.canvaslearningassistant.repository.GraphEdgeRepository;
import org.example.canvaslearningassistant.services.api.CanvasApiService;
import org.example.canvaslearningassistant.services.api.KnowledgeGraphApiService;
import org.example.canvaslearningassistant.services.api.interfaces.QuizApiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final QuizApiService quizApiService;
    private final CanvasApiService canvasApiService;
    private final KnowledgeGraphApiService knowledgeGraphApiService;
    private final GraphEdgeRepository graphEdgeRepository;
    private final CanvasQuizRepository canvasQuizRepository;

    public List<GraphEdge> getGraph(Integer courseId, String accessToken) {

        // Check if graph exists
        List<GraphEdge> existingGraph = graphEdgeRepository.findByCourseId(courseId);
        if (!existingGraph.isEmpty()) {
            log.info("Found existing graph in PostgreSQL for course {}", courseId);
            return existingGraph;
        }

        log.info("No existing graph found. Calling KnowledgeGraph API for course {}", courseId);
        var courseDescription = canvasApiService.getFrontPageContent(courseId, accessToken);
        var graphEdges = knowledgeGraphApiService.generateKnowledgeGraph(courseDescription);

        if (graphEdges.isEmpty()) {
            log.warn("KnowledgeGraph API returned no edges for course {}", courseId);
            return List.of();
        }

        List<GraphEdge> entities = graphEdges.stream()
                .map(dto -> new GraphEdge(null, dto.getFirstNode(), dto.getSecondNode(), dto.getEdge(), courseId))
                .toList();

        // Save all edges
        List<GraphEdge> savedEdges = graphEdgeRepository.saveAll(entities);
        log.info("Saved {} new edges for course {}", savedEdges.size(), courseId);

        return savedEdges;
    }

    @Transactional
    public void updateStudentKnowledge(Integer courseId, Integer studentId, String accessToken) throws QuizGenerationException {
        List<GraphEdge> graphEdges = getGraph(courseId, accessToken);

        String quizTitle = "Knowledge Check Quiz";
        String quizDescription = "Auto-generated quiz based on your course knowledge graph.";
        CanvasQuiz canvasQuiz = canvasApiService.createQuiz(
                courseId,
                quizTitle,
                quizDescription,
                accessToken
        );

        List<CanvasQuizQuestion> createdCanvasQuizQuestions = new ArrayList<>();

        log.info("Calling Quiz API for quiz {}", canvasQuiz.getId());
        List<NewQuizQuestionData> questions = quizApiService.generateQuizQuestions(graphEdges, 1);

        log.info("Creating questions for quiz {}", canvasQuiz.getId());
        for (NewQuizQuestionData question : questions) {
            CanvasQuizQuestion canvasQuizQuestion = canvasApiService.addQuizQuestion(
                    courseId,
                    canvasQuiz.getId(),
                    question,
                    accessToken
            );
            canvasQuizQuestion.setQuiz(canvasQuiz);
            createdCanvasQuizQuestions.add(canvasQuizQuestion);
        }

        canvasQuiz.setQuestions(createdCanvasQuizQuestions);

        log.info("Assigning quiz {} to student {}", canvasQuiz.getId(), studentId);
        canvasApiService.assignQuizToStudent(courseId, canvasQuiz.getId(), studentId, accessToken);
        canvasQuiz.setStudentId(studentId);

        log.info("Publishing quiz {}", canvasQuiz.getId());
        canvasApiService.publishQuiz(courseId, canvasQuiz.getId(), accessToken);

        log.info("Saving quiz {}", canvasQuiz.getId());
        canvasQuizRepository.save(canvasQuiz);
    }
}
