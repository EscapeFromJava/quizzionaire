package com.reske.quizzionaire.service;

import com.reske.quizzionaire.entity.Question;
import com.reske.quizzionaire.model.Session;
import com.reske.quizzionaire.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository repository;

    public List<Question> init(int count) {
        return repository.getRandomQuestions(count);
    }

    public boolean isRightAnswer(Session session, String answer) {
        Question currentQuestion = session.getCurrentQuestion();
        var option = switch (answer) {
            case "/option1" -> currentQuestion.getOption1();
            case "/option2" -> currentQuestion.getOption2();
            case "/option3" -> currentQuestion.getOption3();
            case "/option4" -> currentQuestion.getOption4();
            default -> throw new IllegalStateException("Неизвестный ввод: " + answer);
        };

        return option.equals(currentQuestion.getAnswer());
    }

}
