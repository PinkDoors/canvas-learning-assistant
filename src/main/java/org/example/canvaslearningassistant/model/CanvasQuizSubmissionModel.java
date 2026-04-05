package org.example.canvaslearningassistant.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CanvasQuizSubmissionModel {

    private Integer submissionId;     // Canvas submission ID
    private Integer quizId;           // Canvas quiz ID (assignmentId)
    private Integer studentId;        // Canvas user ID
    private Integer attempt;          // attempt number
    private OffsetDateTime submittedAt;

    private List<CanvasQuizSubmissionAnswerModel> answers; // list of answers
}