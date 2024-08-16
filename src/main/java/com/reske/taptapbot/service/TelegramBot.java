package com.reske.taptapbot.service;

import com.reske.taptapbot.config.BotProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final GameSessionService gameSessionService;

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String chatId = update.getMessage().getChatId().toString();
            String text = update.getMessage().getText();

            if (text.equals("/restart")) {
                gameSessionService.restart(chatId);
            }

            SendMessage result = gameSessionService.sendResult(chatId, text);
            if (result != null) {
                sendMessage(result);
            }

            SendMessage question = gameSessionService.sendQuestion(chatId);
            sendMessage(question);
        }
    }

    @SneakyThrows
    private void sendMessage(SendMessage sendMessage) {
        execute(sendMessage);
    }

}
