package com.reske.taptapbot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Data
@Configuration
@ConfigurationProperties(prefix = "game")
public class GameConfig {

    private String info;
    private Map<Integer, Integer> scoreTable;

}
