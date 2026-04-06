package org.example.canvaslearningassistant.repository;

import org.example.canvaslearningassistant.entity.Concept;
import org.example.canvaslearningassistant.entity.StudentKnowledge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentKnowledgeRepository extends JpaRepository<StudentKnowledge, Long> {

    Optional<StudentKnowledge> findByStudentIdAndConcept(Integer studentId, Concept concept);

    List<StudentKnowledge> findByStudentId(Integer studentId);

    void deleteByStudentId(Integer studentId);
}