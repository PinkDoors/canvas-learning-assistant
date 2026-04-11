package org.example.canvaslearningassistant.controllers;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canvaslearningassistant.dto.GraphSessionData;
import org.example.canvaslearningassistant.entity.GraphEdge;
import org.example.canvaslearningassistant.entity.StudentKnowledge;
import org.example.canvaslearningassistant.services.KnowledgeGraphService;
import org.example.canvaslearningassistant.services.MasteryService;
import org.example.canvaslearningassistant.services.QuizSyncService;
import org.example.canvaslearningassistant.services.api.CanvasApiService;
import org.example.canvaslearningassistant.services.api.KnowledgeGraphApiService;
import org.example.canvaslearningassistant.utils.IdTokenClaimsUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lti")
@Slf4j
public class LtiController {

    private final CanvasApiService canvasApiService;
    private final KnowledgeGraphApiService knowledgeGraphApiService;
    private final KnowledgeGraphService knowledgeGraphService;
    private final MasteryService masteryService;
    private final QuizSyncService quizSyncService;

    private final String accessToken = "3ey3ZnE8ta3Ln8JfknHyN6MNxzXEQP8UNPtucWrWACW2evTeLhu2FwkZf7TeBuAu";

    private final String errorThymeleafTemplate = "error";
    private final String ltiThymeleafTemplate = "graph";
    private final String generatingQuizThymeleafTemplate = "generating-quiz";

    private final String graphSessionDataAttribute = "graph_session_data";

    @PostMapping("/knowledge-graph")
    public String knowledgeGraph(@RequestParam Map<String, String> body,
                                 Model model,
                                 HttpSession session) throws ParseException {
        Optional<String> contextId = IdTokenClaimsUtils.getLtiContextId(body.get("id_token"));
        if (contextId.isEmpty()) {
            model.addAttribute("error", "Missing or invalid LTI context ID");
            return errorThymeleafTemplate;
        }

        Optional<Integer> userId = IdTokenClaimsUtils.getUserId(body.get("id_token"));
        if (userId.isEmpty()) {
            model.addAttribute("error", "Missing or invalid user ID");
            return errorThymeleafTemplate;
        }

        Optional<Integer> courseId = canvasApiService.getCourseId(contextId.get(), accessToken);
        if (courseId.isEmpty()) {
            model.addAttribute("error", "No course id found");
            return errorThymeleafTemplate;
        }

        List<GraphEdge> graphEdges =
                knowledgeGraphService.getGraph(courseId.get(), accessToken);

        GraphSessionData graphSessionData = new GraphSessionData(graphEdges, courseId.get(), userId.get());
        session.setAttribute(graphSessionDataAttribute, graphSessionData);

        return ltiThymeleafTemplate;
    }

    @GetMapping("/graph-data")
    @ResponseBody
    public List<GraphEdge> getGraph(HttpSession session) {
        GraphSessionData data =
                (GraphSessionData) session.getAttribute(graphSessionDataAttribute);

        if (data == null) {
            return List.of();
        }

        return data.graph();
    }

    @PostMapping("/generate-quiz")
    public String generateQuiz() {
        return generatingQuizThymeleafTemplate;
    }

    @PostMapping("/generate-quiz-job")
    public ResponseEntity<?> generateQuizJob(HttpSession session) {
        try {
            GraphSessionData data =
                    (GraphSessionData) session.getAttribute(graphSessionDataAttribute);

            if (data == null) {
                return ResponseEntity
                        .status(400)
                        .body("Session expired");
            }

            knowledgeGraphService.updateStudentKnowledge(
                    data.graph(),
                    data.courseId(),
                    data.userId(),
                    accessToken
            );

            return ResponseEntity.ok("Quiz generated");

        } catch (Exception ex) {
            log.error("Failed to generate quiz", ex);
            return ResponseEntity
                    .status(500)
                    .body("Quiz generation failed");
        }
    }

    @PostMapping("/sync-results")
    public String syncResults(HttpSession session) {
        GraphSessionData data =
                (GraphSessionData) session.getAttribute("graph_session_data");
        quizSyncService.syncAllStudentResults(data.courseId(), data.userId(), accessToken);

        return "graph";
    }

    @PostMapping("/chat")
    @ResponseBody
    public ResponseEntity<Map<String, String>> chat(
            @RequestBody Map<String, String> body,
            HttpSession session) {

        try {
            GraphSessionData data =
                    (GraphSessionData) session.getAttribute("graph_session_data");

            if (data == null) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Missing graph session data"));
            }

            String userMessage = body.get("message");

            // ✅ validate input
            if (userMessage == null || userMessage.isBlank()) {
                return ResponseEntity
                        .badRequest()
                        .body(Map.of("error", "Message is empty"));
            }

            List<StudentKnowledge> studentKnowledges =
                    masteryService.getStudentKnowledge(data.userId());

            String response =
                    knowledgeGraphApiService.ask(userMessage, studentKnowledges);

            return ResponseEntity.ok(Map.of("response", response));

        } catch (Exception e) {
            log.error("Chat error", e);

            return ResponseEntity
                    .status(500)
                    .body(Map.of("error", "Internal server error"));
        }
    }
}
