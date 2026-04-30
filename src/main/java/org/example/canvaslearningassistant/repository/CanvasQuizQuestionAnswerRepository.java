package org.example.canvaslearningassistant.repository;

import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestionAnswer;
import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuizQuestionAnswerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanvasQuizQuestionAnswerRepository extends JpaRepository<CanvasQuizQuestionAnswer, CanvasQuizQuestionAnswerId> {
    List<CanvasQuizQuestionAnswer> findByQuestionId(Integer questionId);
}