package org.example.canvaslearningassistant.controllers;

import lombok.RequiredArgsConstructor;
import org.example.canvaslearningassistant.entities.GraphEdge;
import org.example.canvaslearningassistant.services.KnowledgeGraphService;
import org.example.canvaslearningassistant.services.api.CanvasApiService;
import org.example.canvaslearningassistant.utils.IdTokenClaimsUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
@RequestMapping("/lti")
public class KnowledgeGraphController {

    private final CanvasApiService canvasApiService;
    private final KnowledgeGraphService knowledgeGraphService;

    private final String accessToken = "3ey3ZnE8ta3Ln8JfknHyN6MNxzXEQP8UNPtucWrWACW2evTeLhu2FwkZf7TeBuAu";

    @PostMapping("/knowledge-graph")
    public String knowledgeGraph(@RequestParam Map<String, String> body, Model model) throws ParseException {
        Optional<String> contextId = IdTokenClaimsUtils.getLtiContextId(body.get("id_token"));
        if (contextId.isEmpty()) {
            model.addAttribute("error", "Missing or invalid LTI context ID");
            return "graph";
        }

        Optional<Integer> courseId = canvasApiService.getCourseId(contextId.get(), accessToken);
        if (courseId.isEmpty()) {
            model.addAttribute("error", "No course id found");
            return "graph";
        }

        List<GraphEdge> graphEdges =
                knowledgeGraphService.getGraph(courseId.get(), accessToken);
        model.addAttribute("graph", graphEdges);

        return "graph";
    }
}
