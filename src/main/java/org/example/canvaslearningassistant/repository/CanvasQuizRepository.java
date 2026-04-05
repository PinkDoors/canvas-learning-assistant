package org.example.canvaslearningassistant.repository;

import org.example.canvaslearningassistant.entity.quiz.canvas.CanvasQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CanvasQuizRepository extends JpaRepository<CanvasQuiz, Integer> {
    List<CanvasQuiz> getAllByStudentId(Integer studentId);
}