package com.reske.taptapbot.service;

import com.reske.taptapbot.common.GameConstants;
import com.reske.taptapbot.config.GameConfig;
import com.reske.taptapbot.model.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class MessageService {

    private final ProfileService profileService;
    private final KeyboardService keyboardService;
    private final GameConfig gameConfig;

    public SendMessage getMainMenu(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text("Выберите действие:")
                .replyMarkup(keyboardService.mainMenu())
                .build();
    }

    public SendMessage getStat(Long chatId, Session session) {
        String stat = profileService.getStat(session.getProfile());
        return defaultMessage(chatId, "Ваша статистика: \n" + stat);
    }

    public SendMessage getGreeting(Long chatId, Session session) {
        return defaultMessage(chatId, "Привет, " + session.getProfile().getUserName() + "!");
    }

    public SendMessage getAnswerMenu(Long chatId, Session session) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(session.getCurrentQuestion().getQuestion())
                .replyMarkup(keyboardService.answerMenu(session))
                .build();
    }

    public SendMessage getRightAnswerInfo(Long chatId, Session session) {
        StringBuilder sb = new StringBuilder();
        sb.append("Правильно \uD83D\uDD25\n");
        sb.append("Вы заработали " + gameConfig.getScoreTable().get(session.getLevel()) + " очков\n");
        if (GameConstants.isFireproofLevel(session.getLevel())) {
            sb.append("Достигнута несгораемая сумма!\n");
        }
        sb.append("Следующий вопрос: \n");

        return defaultMessage(chatId, sb.toString());
    }

    public SendMessage getLoseInfo(Long chatId, Session session) {
        String loseInfo = "Неверный ответ \uD83D\uDC4E\n" +
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
