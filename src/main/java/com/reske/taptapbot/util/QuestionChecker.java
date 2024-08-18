package com.reske.taptapbot.util;

import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.repository.QuestionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuestionChecker {

    private final QuestionRepository questionRepository;

    @PostConstruct
    public void init() {
        log.info("Инициализация проверки корректности вопросов");
        List<Question> incorrectQuestions = new ArrayList<>();
        for (Question question : questionRepository.findAll()) {
            String answer = question.getAnswer();
            if (!List.of(question.getOption1(), question.getOption2(), question.getOption3(), question.getOption4()).contains(answer)) {
                incorrectQuestions.add(question);

            }
        }

        if (!incorrectQuestions.isEmpty()) {
            List<Integer> ids = incorrectQuestions.stream()
                    .map(Question::getId)
                    .sorted()
                    .toList();
            throw new RuntimeException("Для вопросов с id = " + ids + " отсутствует правильный ответ в предложенных вариантах");
        }

        log.info("Завершение проверки корректности вопросов");
    }

}
