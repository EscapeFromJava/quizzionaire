package com.reske.taptapbot.service;

import com.reske.taptapbot.common.EmojiConstants;
import com.reske.taptapbot.common.GameConstants;
import com.reske.taptapbot.common.TextConstants;
import com.reske.taptapbot.config.GameConfig;
import com.reske.taptapbot.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class MessageService {

    private final KeyboardService keyboardService;
    private final GameConfig gameConfig;

    public SendMessage getMainMenu(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите действие:")
                .replyMarkup(keyboardService.mainMenu())
                .build();
    }

    public SendMessage getAnswerMenu(Long chatId, Session session) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(session.getCurrentQuestion().getQuestion())
                .replyMarkup(keyboardService.answerMenu(session))
                .build();
    }

    public SendMessage getGreeting(Long chatId, Session session) {
        return defaultMessage(chatId, "Привет " + EmojiConstants.RAISED_HAND + ", " + session.getProfile().getUserName() + "!");
    }

    public SendMessage getRightAnswerInfo(Long chatId, Session session) {
        StringBuilder sb = new StringBuilder();
        sb.append("Правильно ");
        sb.append(EmojiConstants.FIRE);
        sb.append(TextConstants.LINE_BREAK);
        sb.append("Вы заработали ");
        sb.append(gameConfig.getScoreTable().get(session.getLevel()));
        sb.append(" очков ");
        sb.append(EmojiConstants.DIAMOND);
        sb.append(TextConstants.LINE_BREAK);
        if (GameConstants.isFireproofLevel(session.getLevel())) {
            sb.append("Достигнута несгораемая сумма!");
            sb.append(EmojiConstants.FIRE);
            sb.append(TextConstants.LINE_BREAK);
        }
        sb.append("Следующий вопрос ");
        sb.append(EmojiConstants.QUESTION);
        sb.append(":");
        sb.append(TextConstants.LINE_BREAK);

        return defaultMessage(chatId, sb.toString());
    }

    public SendMessage getLoseInfo(Long chatId, Session session) {
        String loseInfo = "Неверный ответ " + EmojiConstants.REVERSED_THUMBS_DOWN + TextConstants.LINE_BREAK +
                          "Правильный - " + session.getCurrentQuestion().getAnswer();
        return defaultMessage(chatId, loseInfo);
    }

    public SendMessage defaultMessage(Long chatId, String message) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(message)
                .build();
    }

    public AnswerCallbackQuery getAnswerCallback(String callbackQueryId) {
        return new AnswerCallbackQuery(callbackQueryId);
    }

}
