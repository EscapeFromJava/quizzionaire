package com.reske.taptapbot.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private Integer correctAnswersCount = 0;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<Integer> passedQuestions = new ArrayList<>();

    public Profile(Long id, String userName) {
        this.id = id;
        this.userName = userName;
    }

}
