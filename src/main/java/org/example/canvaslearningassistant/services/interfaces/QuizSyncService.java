package org.example.canvaslearningassistant.services.interfaces;

import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuiz;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizSubmission;
import org.example.canvaslearningassistant.model.CanvasQuizSubmissionModel;
import org.springframework.transaction.annotation.Transactional;

public interface QuizSyncService {
    @Transactional
    void syncAllStudentResults(Integer courseId, Integer studentId, String accessToken);

    CanvasQuizSubmission mapModelToEntity(CanvasQuizSubmissionModel model, CanvasQuiz quiz);
}
