package com.reske.taptapbot.service;

import com.reske.taptapbot.entity.Profile;
import com.reske.taptapbot.entity.Question;
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
               "Пройдено вопросов: " + profile.getPassedQuestions().size();
    }

    public void addScore(Session session) {
        Integer score = session.getScore();
        Profile profile = session.getProfile();
        profile.setScore(profile.getScore() + score);
        repository.save(profile);
    }

    public void incrementCorrectAnswers(Profile profile) {
        profile.setCorrectAnswersCount(profile.getCorrectAnswersCount() + 1);
        repository.save(profile);
    }

    public void addPassedQuestion(Session session) {
        Profile profile = session.getProfile();
        Question currentQuestion = session.getCurrentQuestion();
        profile.getPassedQuestions().add(currentQuestion.getId());
        repository.save(profile);
    }

}
