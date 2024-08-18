package com.reske.taptapbot.service;

import com.reske.taptapbot.entity.Profile;
import com.reske.taptapbot.model.Session;
import com.reske.taptapbot.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository repository;

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

    public void addScore(Session session) {
        Integer level = session.getLevel();

        Integer score;
        if (level < 5) {
            score = 0;
        } else if (level > 5 && level < 10) {
            score = 1_000;
        } else if (level > 10 && level < 15) {
            score = 32_000;
        } else {
            score = 1_000_000;
        }

        Profile profile = session.getProfile();
        profile.setScore(profile.getScore() + score);

        repository.save(profile);
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
