package com.reske.quizzionaire.controller;

import com.reske.quizzionaire.config.BotProperties;
import com.reske.quizzionaire.config.GameConfig;
import com.reske.quizzionaire.entity.Profile;
import com.reske.quizzionaire.model.Session;
import com.reske.quizzionaire.service.HelpService;
import com.reske.quizzionaire.service.MessageService;
import com.reske.quizzionaire.service.ProfileService;
import com.reske.quizzionaire.service.QuestionService;
import com.reske.quizzionaire.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramController extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final SessionService sessionService;
    private final ProfileService profileService;
    private final HelpService helpService;
    private final GameConfig gameConfig;
    private final MessageService messageService;
    private final QuestionService questionService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            greetingProcess(update);
        } else if (update.hasCallbackQuery()) {
            gameProcess(update);
        }
    }

    private void greetingProcess(Update update) {
        Long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();

        Profile profile = profileService.getOrCreate(chatId, userName);

        Session session = new Session(profile);
        sessionService.addProfile(session);

        sendMessage(messageService.getGreeting(chatId, session));
        sendMessage(messageService.getMainMenu(chatId));
    }

    private void gameProcess(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        String callbackQueryId = update.getCallbackQuery().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        Session session = sessionService.get(chatId);

        switch (callbackData) {
            case "/admin" -> {
                sendMessage(messageService.getAdminInfo(chatId, session));
                sendMessage(messageService.getMainMenu(chatId));
            }
            case "/play" -> {
                sessionService.fillQuestions(session);
                sessionService.setCurrentQuestion(session);
                sendMessage(messageService.getAnswerMenu(chatId, session));
            }
            case "/stat" -> {
                sendMessage(messageService.defaultMessage(chatId, profileService.getStat(session.getProfile())));
                sendMessage(messageService.getMainMenu(chatId));
            }
            case "/info" -> {
                sendMessage(messageService.defaultMessage(chatId, gameConfig.getInfo()));
                sendMessage(messageService.getMainMenu(chatId));
            }
            case "/leaders" -> {
                sendMessage(messageService.defaultMessage(chatId, profileService.getLeadersInfo()));
                sendMessage(messageService.getMainMenu(chatId));
            }
            case "/option1",
                 "/option2",
                 "/option3",
                 "/option4" -> {
                profileService.incrementPassedQuestion(session.getProfile());
                if (questionService.isRightAnswer(session, callbackData)) {
                    sendMessage(messageService.getRightAnswerInfo(chatId, session));
                    sessionService.addScore(session);
                    if (session.getQuestions().isEmpty()) {
                        endGame(session, chatId);
                        sendMessage(messageService.getMainMenu(chatId));
                    } else {
                        sessionService.setCurrentQuestion(session);
                        sendMessage(messageService.getAnswerMenu(chatId, session));
                    }
                } else {
                    sendMessage(messageService.getLoseInfo(chatId, session));
                    endGame(session, chatId);
                    sendMessage(messageService.getMainMenu(chatId));
                }
            }
            case "/help1",
                 "/help2",
                 "/help3" -> {
                handleHelp(chatId, session, callbackData);
                sendMessage(messageService.getAnswerMenu(chatId, session));
            }
            default -> throw new UnsupportedOperationException("Неизвестная команда - " + callbackData);
        }
        sendMessage(messageService.getAnswerCallback(callbackQueryId));
    }

    private void endGame(Session session, Long chatId) {
        String message = sessionService.calculateGameResult(session);
        sendMessage(messageService.defaultMessage(chatId, message));
        sessionService.clear(session);
    }

    private void handleHelp(Long chatId, Session session, String callbackData) {
        switch (callbackData) {
            case "/help1" -> helpService.handleHelp1(session);
            case "/help2" -> {
                String result = helpService.handleHelp2(session);
                sendMessage(messageService.defaultMessage(chatId, result));
            }
            case "/help3" -> {
                String result = helpService.handleHelp3(session);
                sendMessage(messageService.defaultMessage(chatId, result));
            }
            default -> throw new UnsupportedOperationException("Неизвестная команда - " + callbackData);
        }
    }

    @Override
    public String getBotUsername() {
        return botProperties.getName();
    }

    @Override
    public String getBotToken() {
        return botProperties.getToken();
    }

    @SneakyThrows
    private void sendMessage(BotApiMethod<?> message) {
        execute(message);
    }

}
