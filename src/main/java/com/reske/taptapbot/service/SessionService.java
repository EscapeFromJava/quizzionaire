package com.reske.taptapbot.service;

import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SessionService {

    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    private final QuestionService questionService;

    public Session get(Long id) {
        return SESSIONS.get(id);
    }

    public void addProfile(Session session) {
        SESSIONS.put(session.getProfile().getId(), session);
    }

    public void addQuestions(Session session) {
        List<Question> questions = questionService.init(15);
        session.setQuestions(questions);
    }

    public void addScore(Session session) {
        Integer level = session.getLevel();
        int currentValue = switch (level) {
            case 1 -> 100;
            case 2 -> 200;
            case 3 -> 300;
            case 4 -> 500;
            case 5 -> 1_000;
            case 6 -> 2_000;
            case 7 -> 4_000;
            case 8 -> 8_000;
            case 9 -> 16_000;
            case 10 -> 32_000;
            case 11 -> 64_000;
            case 12 -> 125_000;
            case 13 -> 250_000;
            case 14 -> 500_000;
            case 15 -> 1_000_000;
            default -> throw new IllegalStateException("Неизвестное значение: " + level);
        };
        session.setScore(currentValue);
    }

    public void clear(Session session) {
        session.setLevel(0);
        session.setScore(0);
        session.setQuestions(null);
        session.setCurrentQuestion(null);
        session.setHelp1Used(false);
        session.setHelp2Used(false);
        session.setHelp3Used(false);
    }
}
