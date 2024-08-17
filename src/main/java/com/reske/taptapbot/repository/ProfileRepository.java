package com.reske.taptapbot.repository;

import com.reske.taptapbot.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}