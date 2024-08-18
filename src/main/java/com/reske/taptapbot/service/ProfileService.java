package com.reske.taptapbot.service;

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

    private static final Integer FIRST_FIREPROOF_LEVEL = 5;
    private static final Integer SECOND_FIREPROOF_LEVEL = 10;
    private static final Integer WIN_LEVEL = 15;

    private final ProfileRepository repository;
    private final GameConfig gameConfig;

    public Profile getOrCreate(Long id, String userName) {
        return repository.findById(id)
                .orElseGet(() -> repository.save(new Profile(id, userName)));
    }

    public String getStat(Profile profile) {
        return "Пользователь: " + profile.getUserName() + "\n" +
               "Очки: " + profile.getScore() + "\n" +
               "Правильных ответов: " + profile.getCorrectAnswersCount() + "\n" +
               "Пройдено вопросов: " + profile.getPassedQuestions();
    }

    public Integer addAndGetTotalScore(Session session) {
        Map<Integer, Integer> scoreTable = gameConfig.getScoreTable();

        Integer score = switch (session.getLevel() - 1) {
            case 5, 6, 7, 8, 9 -> scoreTable.get(FIRST_FIREPROOF_LEVEL);
            case 10, 11, 12, 13, 14 -> scoreTable.get(SECOND_FIREPROOF_LEVEL);
            case 15 -> scoreTable.get(WIN_LEVEL);
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
