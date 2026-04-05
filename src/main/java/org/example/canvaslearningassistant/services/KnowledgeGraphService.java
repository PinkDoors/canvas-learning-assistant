package org.example.canvaslearningassistant.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canvaslearningassistant.entity.Concept;
import org.example.canvaslearningassistant.entity.GraphEdge;
import org.example.canvaslearningassistant.model.NewQuizQuestionData;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuiz;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestion;
import org.example.canvaslearningassistant.exceptions.QuizGenerationException;
import org.example.canvaslearningassistant.repository.CanvasQuizRepository;
import org.example.canvaslearningassistant.repository.ConceptRepository;
import org.example.canvaslearningassistant.repository.GraphEdgeRepository;
import org.example.canvaslearningassistant.services.api.CanvasApiService;
import org.example.canvaslearningassistant.services.api.KnowledgeGraphApiService;
import org.example.canvaslearningassistant.services.api.interfaces.QuizApiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeGraphService {

    private final QuizApiService quizApiService;
    private final CanvasApiService canvasApiService;
    private final KnowledgeGraphApiService knowledgeGraphApiService;
    private final GraphEdgeRepository graphEdgeRepository;
    private final CanvasQuizRepository canvasQuizRepository;
    private final ConceptRepository conceptRepository;

    @Transactional
    public List<GraphEdge> getGraph(Integer courseId, String accessToken) {

        // 1. Check if graph exists
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

        // 2. Collect all unique concept names
        Set<String> conceptNames = graphEdges.stream()
                .flatMap(dto -> Stream.of(dto.getFirstNode(), dto.getSecondNode()))
                .collect(Collectors.toSet());

        // 3. Load existing concepts in ONE query
        List<Concept> existingConcepts = conceptRepository.findByNameIn(new ArrayList<>(conceptNames));

        Map<String, Concept> conceptMap = existingConcepts.stream()
                .collect(Collectors.toMap(Concept::getName, c -> c));

        // 4. Create missing concepts
        List<Concept> newConcepts = conceptNames.stream()
                .filter(name -> !conceptMap.containsKey(name))
                .map(name -> {
                    var newConcept = new Concept();
                    newConcept.setName(name);
                    return newConcept;
                })
                .toList();

        if (!newConcepts.isEmpty()) {
            conceptRepository.saveAll(newConcepts);
            newConcepts.forEach(c -> conceptMap.put(c.getName(), c));
        }

        // 5. Build GraphEdge entities using managed Concept objects
        List<GraphEdge> entities = graphEdges.stream()
                .map(dto -> new GraphEdge(
                        null,
                        conceptMap.get(dto.getFirstNode()),
                        conceptMap.get(dto.getSecondNode()),
                        dto.getEdge(),
                        courseId
                ))
                .toList();

        // 6. Save all edges
        List<GraphEdge> savedEdges = graphEdgeRepository.saveAll(entities);

        log.info("Saved {} new edges and {} concepts for course {}",
                savedEdges.size(),
                newConcepts.size(),
                courseId
        );

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
