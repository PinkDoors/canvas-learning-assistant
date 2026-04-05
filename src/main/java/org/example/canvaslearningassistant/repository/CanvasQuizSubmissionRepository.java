package org.example.canvaslearningassistant.repository;

import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanvasQuizSubmissionRepository extends JpaRepository<CanvasQuizSubmission, Integer> {

    /**
     * Find all submissions for a given student.
     */
    List<CanvasQuizSubmission> findByStudentId(Integer studentId);

    /**
     * Optionally, find submissions by quiz ID.
     */
    List<CanvasQuizSubmission> findByQuizId(Integer quizId);

    /**
     * Find submissions for a student for a particular quiz.
     */
    List<CanvasQuizSubmission> findByStudentIdAndQuizId(Integer studentId, Integer quizId);
}