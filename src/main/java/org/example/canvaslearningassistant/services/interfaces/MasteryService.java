package org.example.canvaslearningassistant.services.interfaces;

import org.example.canvaslearningassistant.entity.StudentKnowledge;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MasteryService {
    void calculateStudentMastery(Integer studentId);

    List<StudentKnowledge> getStudentKnowledge(Integer studentId);
}
