package org.example.canvaslearningassistant.services;

import org.example.canvaslearningassistant.entity.quiz.canvas.*;
import org.example.canvaslearningassistant.model.CanvasQuizSubmissionModel;
import org.example.canvaslearningassistant.repository.*;
import org.example.canvaslearningassistant.services.api.interfaces.CanvasApiService;
import org.example.canvaslearningassistant.services.interfaces.MasteryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuizSyncServiceImplTest {

    @Mock private CanvasApiService canvasApiService;
    @Mock private MasteryService masteryService;

    @Mock private CanvasQuizRepository canvasQuizRepository;
    @Mock private CanvasQuizSubmissionRepository quizSubmissionRepository;
    @Mock private CanvasQuizQuestionRepository quizQuestionRepository;
    @Mock private CanvasQuizQuestionAnswerRepository quizQuestionAnswerRepository;

    @InjectMocks
    private QuizSyncServiceImpl service;

    @Test
    void syncAllStudentResults_shouldSkip_whenSubmissionAlreadyExists() {
        // Arrange
        Integer courseId = 1;
        Integer studentId = 10;
        String token = "token";

        CanvasQuiz quiz = new CanvasQuiz();
        quiz.setId(100);

        when(canvasQuizRepository.getAllByStudentId(studentId))
                .thenReturn(List.of(quiz));

        CanvasQuizSubmission existing = new CanvasQuizSubmission();
        existing.setQuiz(quiz);

        when(quizSubmissionRepository.findByStudentId(studentId))
                .thenReturn(List.of(existing));

        // Act
        service.syncAllStudentResults(courseId, studentId, token);

        // Assert
        verify(canvasApiService, never()).getSubmissionFromHistoryModel(any(), any(), any(), any());
        verify(quizSubmissionRepository, never()).save(any());
        verify(masteryService).calculateStudentMastery(studentId);
    }

    @Test
    void syncAllStudentResults_shouldDoNothing_whenApiReturnsNull() {
        // Arrange
        Integer courseId = 1;
        Integer studentId = 10;
        String token = "token";

        CanvasQuiz quiz = new CanvasQuiz();
        quiz.setId(100);

        when(canvasQuizRepository.getAllByStudentId(studentId))
                .thenReturn(List.of(quiz));

        when(quizSubmissionRepository.findByStudentId(studentId))
                .thenReturn(Collections.emptyList());

        when(canvasApiService.getSubmissionFromHistoryModel(courseId, 100, studentId, token))
                .thenReturn(null);

        // Act
        service.syncAllStudentResults(courseId, studentId, token);

        // Assert
        verify(quizSubmissionRepository, never()).save(any());
        verify(masteryService).calculateStudentMastery(studentId);
    }

    @Test
    void syncAllStudentResults_shouldSaveSubmission_whenApiReturnsData() {
        // Arrange
        Integer courseId = 1;
        Integer studentId = 10;
        String token = "token";

        CanvasQuiz quiz = new CanvasQuiz();
        quiz.setId(100);

        when(canvasQuizRepository.getAllByStudentId(studentId))
                .thenReturn(List.of(quiz));

        when(quizSubmissionRepository.findByStudentId(studentId))
                .thenReturn(Collections.emptyList());

        CanvasQuizSubmissionModel model = new CanvasQuizSubmissionModel();
        model.setSubmissionId(999);
        model.setStudentId(studentId);
        model.setAttempt(1);
        model.setSubmittedAt(OffsetDateTime.now());
        model.setAnswers(Collections.emptyList());

        when(canvasApiService.getSubmissionFromHistoryModel(courseId, 100, studentId, token))
                .thenReturn(model);

        // Act
        service.syncAllStudentResults(courseId, studentId, token);

        // Assert
        ArgumentCaptor<CanvasQuizSubmission> captor =
                ArgumentCaptor.forClass(CanvasQuizSubmission.class);

        verify(quizSubmissionRepository).save(captor.capture());

        CanvasQuizSubmission saved = captor.getValue();

        assertEquals(999, saved.getId());
        assertEquals(studentId, saved.getStudentId());
        assertEquals(quiz, saved.getQuiz());

        verify(masteryService).calculateStudentMastery(studentId);
    }

    @Test
    void mapModelToEntity_shouldMapBasicFieldsCorrectly() {
        // Arrange
        CanvasQuiz quiz = new CanvasQuiz();
        quiz.setId(10);

        CanvasQuizSubmissionModel model = new CanvasQuizSubmissionModel();
        model.setSubmissionId(1);
        model.setStudentId(5);
        model.setAttempt(2);
        model.setSubmittedAt(OffsetDateTime.parse("2024-01-01T10:00:00Z"));
        model.setAnswers(null);

        // Act
        CanvasQuizSubmission result = service.mapModelToEntity(model, quiz);

        // Assert
        assertEquals(1, result.getId());
        assertEquals(5, result.getStudentId());
        assertEquals(2, result.getAttempt());
        assertEquals(quiz, result.getQuiz());
        assertNotNull(result.getSubmittedAt());
        assertTrue(result.getQuestions() == null || result.getQuestions().isEmpty());
    }
}