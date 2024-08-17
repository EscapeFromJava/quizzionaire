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

}
