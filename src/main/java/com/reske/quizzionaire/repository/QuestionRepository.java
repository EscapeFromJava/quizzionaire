package com.reske.quizzionaire.repository;

import com.reske.quizzionaire.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {

    @Query(value = """
            SELECT *
            FROM question
            ORDER BY RANDOM()
            LIMIT :limit
            """, nativeQuery = true)
    List<Question> getRandomQuestions(@Param("limit") int limit);

}
