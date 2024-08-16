package com.reske.taptapbot.service;

import com.reske.taptapbot.model.QuestionnaireItem;
import com.reske.taptapbot.repository.QuestionnaireItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class QuestionnaireService {

    private static final Random RANDOM = new Random();

    private final QuestionnaireItemRepository repository;

    @PostConstruct
    public void init() {
        QuestionnaireItem q1 = new QuestionnaireItem(
                "Как называется еврейский Новый год?",
                "Рош ха-Шана",
                "Ханука", "Йом Кипур", "Кванза", "Рош ха-Шана"
        );
        QuestionnaireItem q2 = new QuestionnaireItem(
                "Сколько синих полос на флаге США?",
                "0",
                "6", "7", "13", "0");
        QuestionnaireItem q3 = new QuestionnaireItem(
                "Кто из этих персонажей не дружит с Гарри Поттером?",
                "Драко Малфой",
                "Рон Уизли", "Невилл Лонгботтом", "Драко Малфой", "Гермиона Грейнджер");
        repository.saveAll(List.of(q1, q2, q3));
    }

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
        init();
    }

}
