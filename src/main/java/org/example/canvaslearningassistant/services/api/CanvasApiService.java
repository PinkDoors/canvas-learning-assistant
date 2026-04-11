package org.example.canvaslearningassistant.services.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.canvaslearningassistant.configuration.ExternalApiConfiguration;
import org.example.canvaslearningassistant.entity.quiz.canvas.*;
import org.example.canvaslearningassistant.model.CanvasQuizSubmissionQuestionModel;
import org.example.canvaslearningassistant.model.CanvasQuizSubmissionModel;
import org.example.canvaslearningassistant.model.NewQuizQuestionData;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.OffsetDateTime;
import java.util.*;

import org.jsoup.Jsoup;

@Slf4j
@Service
@RequiredArgsConstructor
public class CanvasApiService {

    private final ExternalApiConfiguration externalApiConfiguration;
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String URL_BASE = "/api/v1/courses/";

    public Optional<Integer> getCourseId(String ltiContextId, String accessToken) {
        String url = externalApiConfiguration.getCanvasApiHost() + URL_BASE + "lti_context_id:" + ltiContextId;

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
        String url = externalApiConfiguration.getCanvasApiHost() + URL_BASE + courseId + "/front_page";

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

    public CanvasQuiz createQuiz(Integer courseId,
                                           String title,
                                           String description,
                                           String accessToken) {

        String url = externalApiConfiguration.getCanvasApiHost()
                + URL_BASE + courseId + "/quizzes";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> quiz = Map.of(
                "title", title,
                "description", description,
                "quiz_type", "assignment",   // options: practice_quiz, graded_survey, etc.
                "published", false,
                "only_visible_to_overrides", true
        );

        Map<String, Object> requestBody = Map.of("quiz", quiz);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> body = response.getBody();

        if (body != null && body.containsKey("id")) {
            Integer id = (Integer) body.get("id");
            CanvasQuiz canvasQuiz = new CanvasQuiz();
            canvasQuiz.setId(id);
            return canvasQuiz;
        } else {
            // TODO: exception
            throw new RuntimeException();
        }
    }

    public void publishQuiz(Integer courseId,
                               Integer quizId,
                               String accessToken) {

        String url = externalApiConfiguration.getCanvasApiHost()
                + URL_BASE + courseId + "/quizzes/" + quizId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // Only updating the published flag
        Map<String, Object> quiz = Map.of(
                "published", true
        );

        Map<String, Object> requestBody = Map.of("quiz", quiz);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response;
        response = restTemplate.exchange(
                url,
                HttpMethod.PUT,
                entity,
                Map.class
        );
    }

    public void assignQuizToStudent(Integer courseId,
                                    Integer quizId,
                                    Integer studentId,
                                    String accessToken) {
        // 2️⃣ Create the assignment override for that assignment ID
        String url = externalApiConfiguration.getCanvasApiHost() + URL_BASE + courseId + "/assignments/" + quizId + "/overrides";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, Object> override = Map.of(
                "assignment_override", Map.of(
                        "student_ids", List.of(2),
                        "title", "Override for student " + studentId
                )
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(override, headers);

        restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );
    }

    public CanvasQuizQuestion addQuizQuestion(Integer courseId,
                                              Integer quizId,
                                              NewQuizQuestionData quizQuestion,
                                              String accessToken) {

        String url = externalApiConfiguration.getCanvasApiHost()
                + "/api/v1/courses/" + courseId
                + "/quizzes/" + quizId + "/questions";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        // Build answers array for Canvas API
        List<Map<String, Object>> answersList = new ArrayList<>();
        List<String> optionsList = quizQuestion.getOptions();

        for (int i = 0; i < optionsList.size(); i++) {
            Map<String, Object> answer = new HashMap<>();
            answer.put("text", optionsList.get(i));
            answer.put("weight", quizQuestion.getCorrectIndexes().contains(i) ? 100 : 0);
            answersList.add(answer);
        }

        Map<String, Object> questionPayload = new HashMap<>();
        questionPayload.put("question_name", "Generated Question");
        questionPayload.put("question_text", quizQuestion.getQuestion());
        questionPayload.put("question_type", quizQuestion.getType().getValue());
        questionPayload.put("points_possible", 1);
        questionPayload.put("answers", answersList);

        Map<String, Object> requestBody = Map.of("question", questionPayload);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map<String, Object> responseBody = response.getBody();
        if (responseBody == null) {
            throw new RuntimeException("Failed to create question: response body is null");
        }

        // Map API response to CanvasQuizQuestion entity
        CanvasQuizQuestion canvasQuestion = new CanvasQuizQuestion();
        canvasQuestion.setId((Integer) responseBody.get("id"));
        canvasQuestion.setQuestionName((String) responseBody.get("question_name"));
        canvasQuestion.setQuestionText((String) responseBody.get("question_text"));
        canvasQuestion.setQuestionType((String) responseBody.get("question_type"));

        // Map answers if present
        List<Map<String, Object>> returnedAnswers = (List<Map<String, Object>>) responseBody.get("answers");
        List<CanvasQuizQuestionAnswer> answers = new ArrayList<>();
        if (returnedAnswers != null) {
            for (Map<String, Object> answerMap : returnedAnswers) {
                CanvasQuizQuestionAnswer answer = new CanvasQuizQuestionAnswer();
                answer.setId(new CanvasQuizQuestionAnswerId(canvasQuestion.getId(), (Integer) answerMap.get("id")));
                answer.setText((String) answerMap.get("text"));
                answer.setWeight(((Number)answerMap.get("weight")).intValue());
                answer.setQuestion(canvasQuestion);
                answers.add(answer);
            }
        }
        canvasQuestion.setAnswers(answers);

        return canvasQuestion;
    }

    @Transactional
    public CanvasQuizSubmissionModel getSubmissionFromHistoryModel(
            Integer courseId,
            Integer assignmentId,
            Integer userId,
            String accessToken
    ) {
        String url = externalApiConfiguration.getCanvasApiHost()
                + "/api/v1/courses/" + courseId
                + "/assignments/" + assignmentId
                + "/submissions/" + userId
                + "?include[]=submission_history";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) return null;

        List<Map<String, Object>> history = (List<Map<String, Object>>) response.getBody().get("submission_history");
        if (history == null || history.isEmpty()) return null;

        Map<String, Object> latestAttempt = history.get(history.size() - 1);

        Integer submissionId = (Integer) latestAttempt.get("id");
        Integer attempt = (Integer) latestAttempt.get("attempt");
        OffsetDateTime submittedAt = latestAttempt.get("submitted_at") != null
                ? OffsetDateTime.parse((String) latestAttempt.get("submitted_at"))
                : null;

        List<Map<String, Object>> submissionData = (List<Map<String, Object>>) latestAttempt.get("submission_data");

        List<CanvasQuizSubmissionQuestionModel> canvasQuizSubmissionAnswerModels = null;

        if (submissionData != null) {
            canvasQuizSubmissionAnswerModels = submissionData.stream().map(entry -> {
                List<Integer> answersForQuestion = new ArrayList<>();

                CanvasQuizSubmissionQuestionModel answer = new CanvasQuizSubmissionQuestionModel();
                // handle single and multiple choice answers
                if (entry.containsKey("answer_id")) {
                    answer.setQuestionId((Integer) entry.get("question_id"));
                    answersForQuestion.add((Integer) entry.get("answer_id"));
                }
                for (Map.Entry<String, Object> e : entry.entrySet()) {
                    if (e.getKey().startsWith("answer_") && "1".equals(String.valueOf(e.getValue()))) {
                        answer.setQuestionId((Integer) entry.get("question_id"));
                        answersForQuestion.add(Integer.parseInt(e.getKey().substring(7)));
                    }
                }
                answer.setAnswersIds(answersForQuestion);

                return answer;
            }).toList();
        }

        return new CanvasQuizSubmissionModel(submissionId, assignmentId, userId, attempt, submittedAt, canvasQuizSubmissionAnswerModels);
    }
}
