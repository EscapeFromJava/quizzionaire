package com.reske.taptapbot.common;

import lombok.experimental.UtilityClass;

import java.util.Random;

@UtilityClass
public class GameConstants {

    public static final Integer FIRST_FIREPROOF_LEVEL = 5;
    public static final Integer SECOND_FIREPROOF_LEVEL = 10;
    public static final Integer WIN_LEVEL = 15;
    public static final Random RANDOM = new Random();

    public static boolean isFireproofLevel(int level) {
        return level == FIRST_FIREPROOF_LEVEL || level == SECOND_FIREPROOF_LEVEL || level == WIN_LEVEL;
    }

}
