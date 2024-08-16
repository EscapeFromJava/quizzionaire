package com.reske.taptapbot.service;

import com.reske.taptapbot.model.QuestionnaireItem;
import com.reske.taptapbot.repository.QuestionnaireItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class QuestionnaireService {

    private static final Random RANDOM = new Random();

    private final QuestionnaireItemRepository repository;

    public QuestionnaireItem getRandomItem() {
        List<QuestionnaireItem> items = repository.findByUsed(false);
        System.out.println("Осталось " + items.size() + " вопросов");
        if (items.isEmpty()) {
            return null;
        }

        QuestionnaireItem currentItem = items.get(RANDOM.nextInt(items.size()));
        if (items.size() == 1) {
            currentItem.setLast(true);
        }
        currentItem.setUsed(true);

        repository.save(currentItem);

        return currentItem;
    }

    public boolean checkAnswer(Integer questionId, String userAnswer) {
        QuestionnaireItem questionnaireItem = repository.findById(questionId)
                .orElseThrow(() -> new RuntimeException("Не найден вопрос с id = " + questionId));
        String answer = questionnaireItem.getAnswer();
        return answer.equals(userAnswer);
    }

    public void restart() {
        repository.deleteAll();
    }

}
