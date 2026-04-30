package org.example.canvaslearningassistant.repository;

import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanvasQuizSubmissionRepository extends JpaRepository<CanvasQuizSubmission, Integer> {

    List<CanvasQuizSubmission> findByStudentId(Integer studentId);

    List<CanvasQuizSubmission> findByQuizId(Integer quizId);

    List<CanvasQuizSubmission> findByStudentIdAndQuizId(Integer studentId, Integer quizId);
}