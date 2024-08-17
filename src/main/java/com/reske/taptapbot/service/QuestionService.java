package com.reske.taptapbot.service;

import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.model.Session;
import com.reske.taptapbot.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class QuestionService {

    private static final Random RANDOM = new Random();

    private final QuestionRepository repository;

    public List<Question> init(int count) {
        return repository.getRandomQuestions(count);
    }

    public Question getRandomQuestion(Session session) {
        List<Question> questions = session.getQuestions();

        if (questions.isEmpty()) {
            return null;
        }

        Question currentQuestion = questions.get(RANDOM.nextInt(questions.size()));
        session.setCurrentQuestion(currentQuestion);
        questions.remove(currentQuestion);

        return currentQuestion;
    }

}
