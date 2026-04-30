package org.example.canvaslearningassistant.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizSubmissionModel {

    private Integer submissionId;
    private Integer quizId;
    private Integer studentId;
    private Integer attempt;
    private OffsetDateTime submittedAt;

    private List<CanvasQuizSubmissionQuestionModel> answers;
}