package com.reske.taptapbot.service;

import com.reske.taptapbot.model.QuestionnaireItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GameSessionService {

    private final QuestionnaireService questionnaireService;
    private final Map<String, Integer> userSteps = new HashMap<>();

    public void restart(String chatId) {
        userSteps.remove(chatId);
        questionnaireService.restart();
    }

    public SendMessage sendResult(String chatId, String userAnswer) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        if (userSteps.containsKey(chatId)) {
            Integer questionId = userSteps.get(chatId);
            if (questionId == 0) {
                return null;
            }

            String correctAnswer = questionnaireService.getCorrectAnswer(questionId);
            if (correctAnswer.equals(userAnswer)) {
                sendMessage.setText("Правильно \uD83D\uDD25");
            } else {
                sendMessage.setText("Неверный ответ \uD83D\uDC4E. Правильный - " + correctAnswer);
            }
        } else {
            sendMessage.setText("Добро пожаловать в игру! \uD83D\uDC4B");
        }
        return sendMessage;
    }

    public SendMessage sendQuestion(String chatId) {
        QuestionnaireItem item = questionnaireService.getRandomItem();
        if (item == null) {
            userSteps.put(chatId, 0);
            return new SendMessage(chatId, "Вопросы закончились \uD83D\uDE2D");
        }
        userSteps.put(chatId, item.getId());

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(item.getQuestion());

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);

        if (item.isLast()) {
            replyKeyboardMarkup.setOneTimeKeyboard(true);
        }

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(new KeyboardButton("\uD83C\uDDE6 " + item.getOption1()));
        firstRow.add(new KeyboardButton("\uD83C\uDDE7 " + item.getOption2()));

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(new KeyboardButton("\uD83C\uDDE8 " + item.getOption3()));
        secondRow.add(new KeyboardButton("\uD83C\uDDE9 " + item.getOption4()));

        replyKeyboardMarkup.setKeyboard(List.of(firstRow, secondRow));

        message.setReplyMarkup(replyKeyboardMarkup);

        return message;
    }
}
