package com.reske.quizzionaire.service;

import com.reske.quizzionaire.common.EmojiConstants;
import com.reske.quizzionaire.common.GameConstants;
import com.reske.quizzionaire.config.GameConfig;
import com.reske.quizzionaire.entity.Profile;
import com.reske.quizzionaire.model.Session;
import com.reske.quizzionaire.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProfileService {

    private static final int MAX_TOP_PROFILES = 5;

    private final ProfileRepository repository;
    private final GameConfig gameConfig;
    private final ProfileRepository profileRepository;

    public Profile getOrCreate(Long id, String userName) {
        return repository.findById(id)
                .orElseGet(() -> repository.save(new Profile(id, userName)));
    }

    public String getStat(Profile profile) {
        return "Пользователь " + EmojiConstants.PLAYER + ": " + profile.getUserName() + "\n" +
               "Очки " + EmojiConstants.SCORE + ": " + profile.getScore() + "\n" +
               "Правильных ответов " + EmojiConstants.THUMBS_UP + ": " + profile.getCorrectAnswersCount() + "\n" +
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

    public String getLeadersInfo() {
        StringBuilder sb = new StringBuilder();
        int position = 1;
        for (Profile leader : profileRepository.findLeaders(MAX_TOP_PROFILES)) {
            sb.append("[");
            sb.append(position++);
            sb.append("] ");
            sb.append(leader.getUserName());
            sb.append(": ");
            sb.append(leader.getScore());
            sb.append(" ");
            sb.append(EmojiConstants.DIAMOND);
            sb.append("\n");
        }
        return sb.toString();
    }

}
