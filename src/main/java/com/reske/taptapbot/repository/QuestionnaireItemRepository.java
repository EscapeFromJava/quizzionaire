package com.reske.taptapbot.repository;

import com.reske.taptapbot.model.QuestionnaireItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface QuestionnaireItemRepository extends JpaRepository<QuestionnaireItem, Integer> {

    List<QuestionnaireItem> findByUsed(boolean used);

    @Transactional
    @Modifying
    @Query("UPDATE QuestionnaireItem q SET q.used = FALSE, q.last = FALSE")
    void restart();

}
