package com.reske.taptapbot.repository;

import com.reske.taptapbot.model.QuestionnaireItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionnaireItemRepository extends JpaRepository<QuestionnaireItem, Integer> {

    List<QuestionnaireItem> findByUsed(boolean used);

}
