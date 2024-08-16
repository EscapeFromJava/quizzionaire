package com.reske.taptapbot.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "questionnaire_item")
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class QuestionnaireItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String question;
    private String answer;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private boolean used;
    private boolean last;

}
