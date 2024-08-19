package com.reske.quizzionaire.entity;

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
    @Column(name = "id")
    private Long id;
    @Column(name = "user_name")
    private String userName;
    @Column(name = "score")
    private Integer score = 0;
    @Column(name = "correct_answers_count")
    private Integer correctAnswersCount = 0;
    @Column(name = "passed_questions")
    private Integer passedQuestions = 0;

    public Profile(Long id, String userName) {
        this.id = id;
        this.userName = userName;
    }

}
