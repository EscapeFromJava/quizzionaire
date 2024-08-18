package com.reske.taptapbot.service;

import com.reske.taptapbot.common.EmojiConstants;
import com.reske.taptapbot.common.TextConstants;
import com.reske.taptapbot.config.GameConfig;
import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.model.Session;
import com.reske.taptapbot.util.UtilClass;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.reske.taptapbot.common.GameConstants.WIN_LEVEL;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class SessionService {

    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    private final QuestionService questionService;
    private final ProfileService profileService;
    private final GameConfig gameConfig;

    public Session get(Long id) {
        return SESSIONS.get(id);
    }

    public Map<Long, Session> getSessions() {
        return SESSIONS;
    }

    public void addProfile(Session session) {
        SESSIONS.put(session.getProfile().getId(), session);
    }

    public void fillQuestions(Session session) {
        List<Question> questions = questionService.init(15);
        session.setQuestions(questions);
    }

    public void addScore(Session session) {
        Integer level = session.getLevel();
        Map<Integer, Integer> scoreTable = gameConfig.getScoreTable();
        Integer currentValue = scoreTable.get(level);
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

    public void setCurrentQuestion(Session session) {
        List<Question> questions = session.getQuestions();
        Question currentQuestion = questions.get(UtilClass.RANDOM.nextInt(questions.size()));
        session.setCurrentQuestion(currentQuestion);
        session.setLevel(session.getLevel() + 1);
        questions.remove(currentQuestion);
    }

    public String calculateGameResult(Session session) {
        StringBuilder sb = new StringBuilder("Игра закончилась! ");
        sb.append(EmojiConstants.END);
        sb.append(TextConstants.LINE_BREAK);

        int correctAnswerCount;
        if (session.getQuestions().isEmpty()) {
            correctAnswerCount = WIN_LEVEL;
            sb.append("Ура! Вы выиграли 1000000!");
            sb.append(TextConstants.LINE_BREAK);
            sb.append("Поздравляем! ");
            sb.append(EmojiConstants.WINNER_CUP);
        } else {
            correctAnswerCount = session.getLevel() - 1;
            sb.append("Вы заработали ");
            sb.append(profileService.addAndGetTotalScore(session));
            sb.append(" очков ");
            sb.append(EmojiConstants.DIAMOND);
            sb.append(TextConstants.LINE_BREAK);
            sb.append("Правильных ответов - ");
            sb.append(correctAnswerCount);
            sb.append(" ");
            sb.append(EmojiConstants.SMILLING_FACE);
        }

        profileService.addCorrectAnswers(session.getProfile(), correctAnswerCount);

        return sb.toString();
    }
}
