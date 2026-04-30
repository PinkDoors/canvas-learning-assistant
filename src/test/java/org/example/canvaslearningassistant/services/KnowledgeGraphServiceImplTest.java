package org.example.canvaslearningassistant.services;

import org.example.canvaslearningassistant.dto.GraphEdgeDto;
import org.example.canvaslearningassistant.entity.Concept;
import org.example.canvaslearningassistant.entity.GraphEdge;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuiz;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestion;
import org.example.canvaslearningassistant.exception.QuizGenerationException;
import org.example.canvaslearningassistant.model.NewQuizQuestionData;
import org.example.canvaslearningassistant.repository.CanvasQuizRepository;
import org.example.canvaslearningassistant.repository.ConceptRepository;
import org.example.canvaslearningassistant.repository.GraphEdgeRepository;
import org.example.canvaslearningassistant.services.api.KnowledgeGraphApiServiceImpl;
import org.example.canvaslearningassistant.services.api.interfaces.CanvasApiService;
import org.example.canvaslearningassistant.services.api.interfaces.QuizApiService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class KnowledgeGraphServiceImplTest {

    @Mock
    private QuizApiService quizApiService;

    @Mock
    private CanvasApiService canvasApiService;

    @Mock
    private KnowledgeGraphApiServiceImpl knowledgeGraphApiService;

    @Mock
    private GraphEdgeRepository graphEdgeRepository;

    @Mock
    private CanvasQuizRepository canvasQuizRepository;

    @Mock
    private ConceptRepository conceptRepository;

    @InjectMocks
    private KnowledgeGraphServiceImpl service;

    @Test
    void getGraph_shouldReturnExistingGraph_whenGraphExists() {
        // Arrange
        Integer courseId = 1;
        String token = "token";

        GraphEdge edge = new GraphEdge();
        List<GraphEdge> existingEdges = List.of(edge);

        when(graphEdgeRepository.findByCourseId(courseId)).thenReturn(existingEdges);

        // Act
        List<GraphEdge> result = service.getGraph(courseId, token);

        // Assert
        assertEquals(existingEdges, result);
        verify(graphEdgeRepository).findByCourseId(courseId);
        verifyNoInteractions(canvasApiService, knowledgeGraphApiService);
    }

    @Test
    void getGraph_shouldGenerateAndSaveGraph_whenNoExistingGraph() {
        // Arrange
        Integer courseId = 1;
        String token = "token";

        when(graphEdgeRepository.findByCourseId(courseId)).thenReturn(List.of());
        when(canvasApiService.getFrontPageContent(courseId, token)).thenReturn("content");

        GraphEdgeDto dto = new GraphEdgeDto("A", "B", "rel");

        when(knowledgeGraphApiService.generateKnowledgeGraph("content"))
                .thenReturn(List.of(dto));

        when(conceptRepository.findByNameIn(any())).thenReturn(List.of());

        when(graphEdgeRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<GraphEdge> result = service.getGraph(courseId, token);

        // Assert
        assertEquals(1, result.size());

        GraphEdge saved = result.get(0);
        assertEquals("rel", saved.getEdge());
        assertEquals(courseId, saved.getCourseId());
        assertEquals("A", saved.getFirstNode().getName());
        assertEquals("B", saved.getSecondNode().getName());

        verify(conceptRepository).saveAll(any());
        verify(graphEdgeRepository).saveAll(any());
    }

    @Test
    void getGraph_shouldReuseExistingConcepts_whenConceptsExist() {
        // Arrange
        Integer courseId = 1;
        String token = "token";

        when(graphEdgeRepository.findByCourseId(courseId)).thenReturn(List.of());
        when(canvasApiService.getFrontPageContent(courseId, token)).thenReturn("content");

        GraphEdgeDto dto = new GraphEdgeDto("A", "B", "rel");

        when(knowledgeGraphApiService.generateKnowledgeGraph("content"))
                .thenReturn(List.of(dto));

        Concept existingA = new Concept();
        existingA.setName("A");

        Concept existingB = new Concept();
        existingB.setName("B");

        when(conceptRepository.findByNameIn(any()))
                .thenReturn(List.of(existingA, existingB));

        when(graphEdgeRepository.saveAll(any()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<GraphEdge> result = service.getGraph(courseId, token);

        // Assert
        GraphEdge saved = result.get(0);

        assertSame(existingA, saved.getFirstNode());
        assertSame(existingB, saved.getSecondNode());

        verify(conceptRepository, never()).saveAll(any());
    }

    @Test
    void getGraph_shouldReturnEmptyList_whenApiReturnsNoEdges() {
        // Arrange
        Integer courseId = 1;
        String token = "token";

        when(graphEdgeRepository.findByCourseId(courseId)).thenReturn(List.of());
        when(canvasApiService.getFrontPageContent(courseId, token)).thenReturn("content");
        when(knowledgeGraphApiService.generateKnowledgeGraph("content"))
                .thenReturn(List.of());

        // Act
        List<GraphEdge> result = service.getGraph(courseId, token);

        // Assert
        assertTrue(result.isEmpty());
        verify(graphEdgeRepository, never()).saveAll(any());
    }

    @Test
    void updateStudentKnowledge_shouldCreateAssignPublishAndSaveQuiz() throws QuizGenerationException {
        // Arrange
        Integer courseId = 1;
        Integer studentId = 10;
        String token = "token";

        CanvasQuiz quiz = new CanvasQuiz();
        quiz.setId(100);

        when(canvasApiService.createQuiz(any(), any(), any(), any()))
                .thenReturn(quiz);

        NewQuizQuestionData questionData = new NewQuizQuestionData();
        questionData.setConcept("Concept1");

        when(quizApiService.generateQuizQuestions(any(), eq(1)))
                .thenReturn(List.of(questionData));

        Concept concept = new Concept();
        concept.setName("Concept1");

        when(conceptRepository.findByName("Concept1"))
                .thenReturn(Optional.of(concept));

        CanvasQuizQuestion question = new CanvasQuizQuestion();

        when(canvasApiService.addQuizQuestion(any(), any(), any(), any()))
                .thenReturn(question);

        // Act
        service.updateStudentKnowledge(List.of(), courseId, studentId, token);

        // Assert
        verify(canvasApiService).createQuiz(eq(courseId), any(), any(), eq(token));
        verify(canvasApiService).addQuizQuestion(eq(courseId), eq(100), any(), eq(token));
        verify(canvasApiService).assignQuizToStudent(courseId, 100, studentId, token);
        verify(canvasApiService).publishQuiz(courseId, 100, token);
        verify(canvasQuizRepository).save(quiz);

        assertEquals(studentId, quiz.getStudentId());
        assertEquals(1, quiz.getQuestions().size());
    }

    @Test
    void updateStudentKnowledge_shouldThrowException_whenConceptNotFound() {
        // Arrange
        Integer courseId = 1;
        Integer studentId = 10;
        String token = "token";

        CanvasQuiz quiz = new CanvasQuiz();
        quiz.setId(100);

        when(canvasApiService.createQuiz(any(), any(), any(), any()))
                .thenReturn(quiz);

        NewQuizQuestionData questionData = new NewQuizQuestionData();
        questionData.setConcept("MissingConcept");

        when(quizApiService.generateQuizQuestions(any(), anyInt()))
                .thenReturn(List.of(questionData));

        when(conceptRepository.findByName("MissingConcept"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                service.updateStudentKnowledge(List.of(), courseId, studentId, token)
        );

        verify(canvasQuizRepository, never()).save(any());
    }
}