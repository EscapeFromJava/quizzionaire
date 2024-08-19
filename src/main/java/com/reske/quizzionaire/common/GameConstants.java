package com.reske.quizzionaire.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class GameConstants {

    public static final Integer FIRST_FIREPROOF_LEVEL = 5;
    public static final Integer SECOND_FIREPROOF_LEVEL = 10;
    public static final Integer WIN_LEVEL = 15;

    public static boolean isFireproofLevel(int level) {
        return level == FIRST_FIREPROOF_LEVEL || level == SECOND_FIREPROOF_LEVEL || level == WIN_LEVEL;
    }

}
