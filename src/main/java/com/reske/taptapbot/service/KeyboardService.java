package com.reske.taptapbot.service;

import com.reske.taptapbot.config.KeyboardConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Component
@RequiredArgsConstructor
public class KeyboardService {

    private final KeyboardConfiguration keyboardConfiguration;

    public InlineKeyboardMarkup mainMenu() {
        return new InlineKeyboardMarkup(
                List.of(
                        keyboardConfiguration.getMainMenu().entrySet().stream()
                                .map(entry -> {
                                    InlineKeyboardButton button = new InlineKeyboardButton();
                                    button.setText(entry.getKey());
                                    button.setCallbackData(entry.getValue());
                                    return button;
                                }).toList()));
    }

}
