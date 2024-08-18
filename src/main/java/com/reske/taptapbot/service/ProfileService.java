package com.reske.taptapbot.service;

import com.reske.taptapbot.common.EmojiConstants;
import com.reske.taptapbot.common.GameConstants;
import com.reske.taptapbot.common.TextConstants;
import com.reske.taptapbot.config.GameConfig;
import com.reske.taptapbot.entity.Profile;
import com.reske.taptapbot.model.Session;
import com.reske.taptapbot.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository repository;
    private final GameConfig gameConfig;

    public Profile getOrCreate(Long id, String userName) {
        return repository.findById(id)
                .orElseGet(() -> repository.save(new Profile(id, userName)));
    }

    public String getStat(Profile profile) {
        return "Пользователь " + EmojiConstants.PLAYER + ": " + profile.getUserName() + TextConstants.LINE_BREAK +
               "Очки " + EmojiConstants.SCORE + ": " + profile.getScore() + TextConstants.LINE_BREAK +
               "Правильных ответов " + EmojiConstants.THUMBS_UP + ": " + profile.getCorrectAnswersCount() + TextConstants.LINE_BREAK +
               "Пройдено вопросов " + EmojiConstants.QUESTION + ": " + profile.getPassedQuestions();
    }

    public Integer addAndGetTotalScore(Session session) {
        Map<Integer, Integer> scoreTable = gameConfig.getScoreTable();

        Integer score = switch (session.getLevel() - 1) {
            case 5, 6, 7, 8, 9 -> scoreTable.get(GameConstants.FIRST_FIREPROOF_LEVEL);
            case 10, 11, 12, 13, 14 -> scoreTable.get(GameConstants.SECOND_FIREPROOF_LEVEL);
            case 15 -> scoreTable.get(GameConstants.WIN_LEVEL);
            default -> 0;
        };

        Profile profile = session.getProfile();
        profile.setScore(profile.getScore() + score);

        repository.save(profile);

        return score;
    }

    public void incrementPassedQuestion(Profile profile) {
        profile.setPassedQuestions(profile.getPassedQuestions() + 1);
        repository.save(profile);
    }

    public void addCorrectAnswers(Profile profile, int correctAnswerCount) {
        profile.setCorrectAnswersCount(profile.getCorrectAnswersCount() + correctAnswerCount);
        repository.save(profile);
    }
}
