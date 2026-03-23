package org.example.canvaslearningassistant.services.api;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jsoup.Jsoup;

@Service
public class CanvasApiService {
    private static final String CANVAS_API_BASE = "http://canvas.docker/api/v1/courses/";
    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<Integer> getCourseId(String ltiContextId, String accessToken) {
        String url = CANVAS_API_BASE + "lti_context_id:" + ltiContextId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);   // Canvas OAuth2 token
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("id")) {
            return Optional.of((Integer) body.get("id"));
        } else {
            return Optional.empty();
        }
    }

    public String getFrontPageContent(Integer courseId, String accessToken) {
        String url = CANVAS_API_BASE + courseId + "/front_page";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);   // Canvas OAuth2 token
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("body")) {
            String html = (String) body.get("body"); // HTML content
            return Jsoup.parse(html).text();
        } else {
            return "";
        }
    }
}
