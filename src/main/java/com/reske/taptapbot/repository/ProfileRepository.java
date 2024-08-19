package com.reske.taptapbot.repository;

import com.reske.taptapbot.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {

    @Query(value = """
            SELECT *
            FROM profile
            ORDER BY score DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Profile> findLeaders(@Param("limit") int limit);

}