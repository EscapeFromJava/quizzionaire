package com.reske.taptapbot.service;

import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.repository.QuestionRepository;
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

}
