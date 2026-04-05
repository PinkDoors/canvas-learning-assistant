//package org.example.canvaslearningassistant.services;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.Map;
//import java.util.UUID;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class MasteryService {
//
//    private final StudentAnswerRepository studentAnswerRepository;
//
//    public Map<String, Double> computeMastery(UUID studentId) {
//        List<QuizSubmissionAnswers> answers = studentAnswerRepository.findByStudentId(studentId);
//
//        // Group by concept
//        return answers.stream()
//                .collect(Collectors.groupingBy(
//                        ans -> ans.getQuestion().getConcept(),
//                        Collectors.averagingDouble(ans -> ans.getIsCorrect() ? 1.0 : 0.0)
//                ));
//    }
//}