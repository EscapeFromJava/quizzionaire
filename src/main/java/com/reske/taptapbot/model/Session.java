package com.reske.taptapbot.model;

import com.reske.taptapbot.entity.Profile;
import com.reske.taptapbot.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class Session {
    private Profile profile;
    private Question currentQuestion;
    private List<Question> questions;

    public Session(Profile profile) {
        this.profile = profile;
    }

}
