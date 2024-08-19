package com.reske.quizzionaire.model;

import com.reske.quizzionaire.entity.Profile;
import com.reske.quizzionaire.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class Session {
    private Profile profile;
    private Question currentQuestion;
    private Integer level;
    private Integer score;
    private List<Question> questions;
    private boolean help1Used;
    private boolean help2Used;
    private boolean help3Used;

    public Session(Profile profile) {
        this.profile = profile;
        this.level = 0;
        this.score = 0;
    }

}
