package com.reske.taptapbot.service;

import com.reske.taptapbot.config.KeyboardConfiguration;
import com.reske.taptapbot.config.KeyboardConfiguration.Option;
import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeyboardService {

    private final KeyboardConfiguration keyboardConfiguration;

    public InlineKeyboardMarkup mainMenu() {
        return new InlineKeyboardMarkup(
                List.of(
                        keyboardConfiguration.getMainMenu().entrySet().stream()
                                .map(entry -> InlineKeyboardButton.builder()
                                        .text(entry.getKey())
                                        .callbackData(entry.getValue())
                                        .build()
                                ).toList()));
    }

    public InlineKeyboardMarkup answerMenu(Session session) {
        Question currentQuestion = session.getCurrentQuestion();
        Map<String, Option> optionMenu = keyboardConfiguration.getOptionMenu();

        List<InlineKeyboardButton> firstRowOptions = new ArrayList<>();
        if (currentQuestion.getOption1() != null) {
            InlineKeyboardButton button = generateOption(optionMenu.get("option1"), currentQuestion.getOption1());
            firstRowOptions.add(button);
        }
        if (currentQuestion.getOption2() != null) {
            InlineKeyboardButton button = generateOption(optionMenu.get("option2"), currentQuestion.getOption2());
            firstRowOptions.add(button);
        }

        List<InlineKeyboardButton> secondRowOptions = new ArrayList<>();
        if (currentQuestion.getOption3() != null) {
            InlineKeyboardButton button = generateOption(optionMenu.get("option3"), currentQuestion.getOption3());
            secondRowOptions.add(button);
        }
        if (currentQuestion.getOption4() != null) {
            InlineKeyboardButton button = generateOption(optionMenu.get("option4"), currentQuestion.getOption4());
            secondRowOptions.add(button);
        }

        List<InlineKeyboardButton> helpRow = getHelpMenu(session);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(firstRowOptions);
        rows.add(secondRowOptions);
        if (!helpRow.isEmpty()) {
            rows.add(helpRow);
        }

        return new InlineKeyboardMarkup(rows);
    }

    private List<InlineKeyboardButton> getHelpMenu(Session session) {
        Map<String, Option> helpMenu = keyboardConfiguration.getHelpMenu();

        List<InlineKeyboardButton> row = new ArrayList<>();

        if (!session.isHelp1Used()) {
            InlineKeyboardButton button = generateOption(helpMenu.get("help1"), null);
            row.add(button);
        }
        if (!session.isHelp2Used()) {
            InlineKeyboardButton button = generateOption(helpMenu.get("help2"), null);
            row.add(button);
        }
        if (!session.isHelp3Used()) {
            InlineKeyboardButton button = generateOption(helpMenu.get("help3"), null);
            row.add(button);
        }

        return row;
    }

    private InlineKeyboardButton generateOption(Option option, String optionValue) {
        return InlineKeyboardButton.builder()
                .text(option.getText() + Objects.requireNonNullElse(optionValue, ""))
                .callbackData(option.getCallbackData())
                .build();
    }

}
