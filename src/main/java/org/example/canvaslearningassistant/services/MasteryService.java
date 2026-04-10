package org.example.canvaslearningassistant.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canvaslearningassistant.entity.Concept;
import org.example.canvaslearningassistant.entity.StudentKnowledge;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.example.canvaslearningassistant.entity.quiz.canvas.*;

import org.example.canvaslearningassistant.repository.*;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MasteryService {

    private final CanvasQuizSubmissionRepository submissionRepository;
    private final StudentKnowledgeRepository knowledgeRepository;

    @Transactional
    public void calculateStudentMastery(Integer studentId) {

        log.info("Calculating mastery for student {}", studentId);

        List<CanvasQuizSubmission> submissions = submissionRepository.findByStudentId(studentId);

        if (submissions.isEmpty()) {
            log.warn("No submissions found for student {}", studentId);
            return;
        }

        // Clear previous mastery
        knowledgeRepository.deleteByStudentId(studentId);

        Map<Concept, int[]> conceptScores = new HashMap<>(); // concept → [earned, total]

        for (CanvasQuizSubmission submission : submissions) {
            for (CanvasQuizSubmissionQuestion questionResponse : submission.getQuestions()) {

                CanvasQuizQuestion question = questionResponse.getQuestion();
                if (question == null || question.getConcept() == null) continue;

                Concept concept = question.getConcept();
                int earned = 0;
                int total = 1; // each question counts as 1

                // Get all correct answers
                Set<Integer> correctAnswerIds = question.getAnswers().stream()
                        .filter(a -> a.getWeight() != null && a.getWeight() == 100)
                        .map(a -> a.getId().getAnswerId())
                        .collect(Collectors.toSet());

                // Get selected answer IDs
                Set<Integer> selectedAnswerIds = questionResponse.getSelectedAnswers().stream()
                        .map(a -> a.getId().getAnswerId())
                        .collect(Collectors.toSet());

                // ✅ For multiple-choice: student must select ALL correct answers, and NO wrong ones
                if (!correctAnswerIds.isEmpty() && selectedAnswerIds.equals(correctAnswerIds)) {
                    earned = 1;
                }

                conceptScores.computeIfAbsent(concept, c -> new int[2]);
                conceptScores.get(concept)[0] += earned;
                conceptScores.get(concept)[1] += total;
            }
        }

        // Save mastery
        List<StudentKnowledge> knowledgeList = new ArrayList<>();
        for (Map.Entry<Concept, int[]> entry : conceptScores.entrySet()) {
            Concept concept = entry.getKey();
            int earned = entry.getValue()[0];
            int total = entry.getValue()[1];
            int mastery = total == 0 ? 0 : (int) ((earned * 100.0) / total);

            StudentKnowledge sk = new StudentKnowledge();
            sk.setStudentId(studentId);
            sk.setConcept(concept);
            sk.setMasteryLevel(mastery);

            knowledgeList.add(sk);
        }

        knowledgeRepository.saveAll(knowledgeList);
        log.info("Saved {} mastery records for student {}", knowledgeList.size(), studentId);
    }

    @Transactional(readOnly = true)
    public List<StudentKnowledge> getStudentKnowledge(Integer studentId) {
        return knowledgeRepository.findByStudentId(studentId);
    }
}