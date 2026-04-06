package org.example.canvaslearningassistant.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canvaslearningassistant.entity.quiz.canvas.*;
import org.example.canvaslearningassistant.model.CanvasQuizSubmissionModel;
import org.example.canvaslearningassistant.repository.CanvasQuizQuestionAnswerRepository;
import org.example.canvaslearningassistant.repository.CanvasQuizQuestionRepository;
import org.example.canvaslearningassistant.repository.CanvasQuizRepository;
import org.example.canvaslearningassistant.repository.CanvasQuizSubmissionRepository;
import org.example.canvaslearningassistant.services.api.CanvasApiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuizSyncService {

    private final CanvasApiService canvasApiService;
    private final MasteryService masteryService;

    private final CanvasQuizRepository canvasQuizRepository;
    private final CanvasQuizSubmissionRepository quizSubmissionRepository;

    private final CanvasQuizQuestionRepository quizQuestionRepository;
    private final CanvasQuizQuestionAnswerRepository quizQuestionAnswerRepository;

    @Transactional
    public void syncAllStudentResults(Integer courseId, Integer studentId, String accessToken) {
        // 1. Fetch all distinct quiz IDs from stored questions
        List<CanvasQuiz> allQuizes = canvasQuizRepository.getAllByStudentId(studentId);
        List<Integer> allQuizIds = allQuizes.stream().map(CanvasQuiz::getId).toList();

        // 2. For each quiz, fetch submission directly for this student
        allQuizIds.stream()
                .distinct()
                .forEach(quizId -> {

                    // 2a. Skip if this student already has a submission stored
                    boolean attemptExists = quizSubmissionRepository.findByStudentId(studentId)
                            .stream()
                            .anyMatch(a -> a.getQuiz().getId() != null && a.getQuiz().getId().equals(quizId));

                    if (attemptExists) return;

                    // 2b. Fetch submission from Canvas API (returns model)
                    CanvasQuizSubmissionModel submissionModel = canvasApiService
                            .getSubmissionFromHistoryModel(courseId, quizId, studentId, accessToken);

                    if (submissionModel == null) return;

                    // 3. Map model to entity
                    // TODO: change exception
                    CanvasQuiz quiz = allQuizes.stream().filter(x -> Objects.equals(x.getId(), quizId)).findFirst().orElseThrow(() -> new RuntimeException("Quiz not found"));
                    CanvasQuizSubmission submissionEntity = mapModelToEntity(submissionModel, quiz);

                    // 4. Save submission along with answers (cascade)
                    quizSubmissionRepository.save(submissionEntity);

                    log.info("Saved submission {} for student {}", submissionEntity.getId(), studentId);
                });

        log.info("Finished syncing quiz results for student {}", studentId);

        masteryService.calculateStudentMastery(studentId);
    }

    /**
     * Map CanvasQuizSubmissionModel -> CanvasQuizSubmission entity
     */
    private CanvasQuizSubmission mapModelToEntity(CanvasQuizSubmissionModel model, CanvasQuiz quiz) {
        CanvasQuizSubmission entity = new CanvasQuizSubmission();
        entity.setId(model.getSubmissionId());
        entity.setQuiz(quiz);
        entity.setStudentId(model.getStudentId());
        entity.setAttempt(model.getAttempt());
        entity.setSubmittedAt(model.getSubmittedAt() != null ? model.getSubmittedAt() : OffsetDateTime.now());

        if (model.getAnswers() != null) {
            List<CanvasQuizSubmissionQuestion> answerEntities = model.getAnswers()
                    .stream()
                    .map(a -> {
                        CanvasQuizSubmissionQuestion questionResponse = new CanvasQuizSubmissionQuestion();

                        CanvasQuizQuestion question = quizQuestionRepository.findById(a.getQuestionId()).orElseThrow(() -> new RuntimeException("Question not found"));

                        List<CanvasQuizQuestionAnswer> answers = new ArrayList<>();
                        for (Integer answerId : a.getAnswersIds()) {
                            answers.add(quizQuestionAnswerRepository.findById(new CanvasQuizQuestionAnswerId(question.getId(), answerId)).orElseThrow(() -> new RuntimeException("Question not found")));
                        }

                        questionResponse.setQuestion(question);
                        questionResponse.setSelectedAnswers(answers);
                        questionResponse.setSubmission(entity);

                        return questionResponse;
                    }).collect(Collectors.toList());

            entity.setQuestions(answerEntities);
        }

        return entity;
    }
}