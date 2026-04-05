package org.example.canvaslearningassistant.repository;

import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanvasQuizQuestionRepository extends JpaRepository<CanvasQuizQuestion, Integer> {
    // Optional: find all questions for a quiz
    List<CanvasQuizQuestion> findByQuizId(Integer quizId);
}