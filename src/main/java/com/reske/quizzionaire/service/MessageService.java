package com.reske.quizzionaire.service;

import com.reske.quizzionaire.common.EmojiConstants;
import com.reske.quizzionaire.common.GameConstants;
import com.reske.quizzionaire.config.GameConfig;
import com.reske.quizzionaire.model.Session;
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
        sb.append("\n");
        sb.append("Вы заработали ");
        sb.append(gameConfig.getScoreTable().get(session.getLevel()));
        sb.append(" очков ");
        sb.append(EmojiConstants.DIAMOND);
        sb.append("\n");
        if (GameConstants.isFireproofLevel(session.getLevel())) {
            sb.append("Достигнута несгораемая сумма!");
            sb.append(EmojiConstants.FIRE);
            sb.append("\n");
        }
        sb.append("Следующий вопрос ");
        sb.append(EmojiConstants.QUESTION);
        sb.append(":\n");

        return defaultMessage(chatId, sb.toString());
    }

    public SendMessage getLoseInfo(Long chatId, Session session) {
        String loseInfo = "Неверный ответ " + EmojiConstants.REVERSED_THUMBS_DOWN + "\n" +
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
