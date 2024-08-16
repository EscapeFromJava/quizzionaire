package com.reske.taptapbot.test;

import com.reske.taptapbot.repository.QuestionnaireItemRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Slf4j
@Profile("test")
@Component
@RequiredArgsConstructor
public class Restarter {

    private final QuestionnaireItemRepository repository;

    @PostConstruct
    public void init() {
        log.info("Подключен профиль test. Производится сброс конфигруации вопросов");
        repository.restart();
    }

}
