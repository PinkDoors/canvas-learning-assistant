package org.example.canvaslearningassistant.services.api.interfaces;

import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuiz;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestion;
import org.example.canvaslearningassistant.model.CanvasQuizSubmissionModel;
import org.example.canvaslearningassistant.model.NewQuizQuestionData;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface CanvasApiService {
    Optional<Integer> getCourseId(String ltiContextId, String accessToken);

    String getFrontPageContent(Integer courseId, String accessToken);

    CanvasQuiz createQuiz(Integer courseId,
                          String title,
                          String description,
                          String accessToken);

    void publishQuiz(Integer courseId,
                     Integer quizId,
                     String accessToken);

    void assignQuizToStudent(Integer courseId,
                             Integer quizId,
                             Integer studentId,
                             String accessToken);

    CanvasQuizQuestion addQuizQuestion(Integer courseId,
                                       Integer quizId,
                                       NewQuizQuestionData quizQuestion,
                                       String accessToken);

    @Transactional
    CanvasQuizSubmissionModel getSubmissionFromHistoryModel(
            Integer courseId,
            Integer assignmentId,
            Integer userId,
            String accessToken
    );
}
