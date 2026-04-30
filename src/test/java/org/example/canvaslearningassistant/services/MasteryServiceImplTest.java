package org.example.canvaslearningassistant.services;

import org.example.canvaslearningassistant.entity.Concept;
import org.example.canvaslearningassistant.entity.StudentKnowledge;
import org.example.canvaslearningassistant.entity.quiz.canvas.*;
import org.example.canvaslearningassistant.repository.CanvasQuizSubmissionRepository;
import org.example.canvaslearningassistant.repository.StudentKnowledgeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MasteryServiceImplTest {

    @Mock
    private CanvasQuizSubmissionRepository submissionRepository;

    @Mock
    private StudentKnowledgeRepository knowledgeRepository;

    @InjectMocks
    private MasteryServiceImpl masteryService;

    private Concept concept;

    @BeforeEach
    void setUp() {
        concept = new Concept(1L, "Algebra");
    }

    @Test
    void calculateStudentMastery_shouldReturnEarly_whenNoSubmissions() {
        // Arrange
        Integer studentId = 1;
        when(submissionRepository.findByStudentId(studentId)).thenReturn(Collections.emptyList());

        // Act
        masteryService.calculateStudentMastery(studentId);

        // Assert
        verify(submissionRepository).findByStudentId(studentId);
        verifyNoInteractions(knowledgeRepository);
    }

    @Test
    void calculateStudentMastery_shouldSavePerfectScore_whenAnswerIsCorrect() {
        // Arrange
        Integer studentId = 1;

        CanvasQuizQuestionAnswer correctAnswer = new CanvasQuizQuestionAnswer();
        correctAnswer.setId(new CanvasQuizQuestionAnswerId(1, 1));
        correctAnswer.setWeight(100);

        CanvasQuizQuestion question = new CanvasQuizQuestion();
        question.setId(1);
        question.setConcept(concept);
        question.setAnswers(List.of(correctAnswer));

        CanvasQuizSubmissionQuestion submissionQuestion = new CanvasQuizSubmissionQuestion();
        submissionQuestion.setQuestion(question);
        submissionQuestion.setSelectedAnswers(List.of(correctAnswer));

        CanvasQuizSubmission submission = new CanvasQuizSubmission();
        submission.setStudentId(studentId);
        submission.setQuestions(List.of(submissionQuestion));

        when(submissionRepository.findByStudentId(studentId))
                .thenReturn(List.of(submission));

        // Act
        masteryService.calculateStudentMastery(studentId);

        // Assert
        ArgumentCaptor<List<StudentKnowledge>> captor = ArgumentCaptor.forClass(List.class);
        verify(knowledgeRepository).saveAll(captor.capture());

        List<StudentKnowledge> saved = captor.getValue();
        assertEquals(1, saved.size());
        assertEquals(100, saved.get(0).getMasteryLevel());
        assertEquals(concept, saved.get(0).getConcept());
    }

    @Test
    void calculateStudentMastery_shouldSaveZero_whenAnswerIsIncorrect() {
        // Arrange
        Integer studentId = 1;

        CanvasQuizQuestionAnswer correctAnswer = new CanvasQuizQuestionAnswer();
        correctAnswer.setId(new CanvasQuizQuestionAnswerId(1, 1));
        correctAnswer.setWeight(100);

        CanvasQuizQuestionAnswer wrongAnswer = new CanvasQuizQuestionAnswer();
        wrongAnswer.setId(new CanvasQuizQuestionAnswerId(1, 2));
        wrongAnswer.setWeight(0);

        CanvasQuizQuestion question = new CanvasQuizQuestion();
        question.setId(1);
        question.setConcept(concept);
        question.setAnswers(List.of(correctAnswer, wrongAnswer));

        CanvasQuizSubmissionQuestion submissionQuestion = new CanvasQuizSubmissionQuestion();
        submissionQuestion.setQuestion(question);
        submissionQuestion.setSelectedAnswers(List.of(wrongAnswer));

        CanvasQuizSubmission submission = new CanvasQuizSubmission();
        submission.setStudentId(studentId);
        submission.setQuestions(List.of(submissionQuestion));

        when(submissionRepository.findByStudentId(studentId))
                .thenReturn(List.of(submission));

        // Act
        masteryService.calculateStudentMastery(studentId);

        // Assert
        ArgumentCaptor<List<StudentKnowledge>> captor = ArgumentCaptor.forClass(List.class);
        verify(knowledgeRepository).saveAll(captor.capture());

        StudentKnowledge result = captor.getValue().get(0);
        assertEquals(0, result.getMasteryLevel());
    }

    @Test
    void calculateStudentMastery_shouldDeleteOldKnowledgeBeforeSaving() {
        // Arrange
        Integer studentId = 1;

        CanvasQuizSubmission submission = new CanvasQuizSubmission();
        submission.setStudentId(studentId);
        submission.setQuestions(Collections.emptyList());

        when(submissionRepository.findByStudentId(studentId))
                .thenReturn(List.of(submission));

        // Act
        masteryService.calculateStudentMastery(studentId);

        // Assert
        verify(knowledgeRepository).deleteByStudentId(studentId);
        verify(knowledgeRepository).saveAll(anyList());
    }
}