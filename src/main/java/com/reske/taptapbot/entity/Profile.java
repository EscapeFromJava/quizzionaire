package com.reske.taptapbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "profile")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Profile {

    @Id
    private Long id;
    @Column(name = "user_name")
    private String userName;
    private Integer score = 0;
    private Integer correctAnswersCount = 0;
    private Integer passedQuestions = 0;

    public Profile(Long id, String userName) {
        this.id = id;
        this.userName = userName;
    }

}
