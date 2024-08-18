package com.reske.taptapbot.controller;

import com.reske.taptapbot.common.GameConstants;
import com.reske.taptapbot.config.BotProperties;
import com.reske.taptapbot.config.GameConfig;
import com.reske.taptapbot.entity.Profile;
import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.model.Session;
import com.reske.taptapbot.service.HelpService;
import com.reske.taptapbot.service.KeyboardService;
import com.reske.taptapbot.service.ProfileService;
import com.reske.taptapbot.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import static com.reske.taptapbot.common.GameConstants.WIN_LEVEL;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramController extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final SessionService sessionService;
    private final ProfileService profileService;
    private final HelpService helpService;
    private final GameConfig gameConfig;
    private final KeyboardService keyboardService;

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

        sendGreeting(chatId, userName);
        sendMenu(chatId);
    }

    private void gameProcess(Update update) {
        String callbackData = update.getCallbackQuery().getData();
        String callbackQueryId = update.getCallbackQuery().getId();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        Session session = sessionService.get(chatId);

        switch (callbackData) {
            case "/play" -> {
                sessionService.fillQuestions(session);
                sessionService.setCurrentQuestion(session);
                showAnswerOptions(session, chatId);
            }
            case "/stat" -> {
                String stat = profileService.getStat(session.getProfile());
                sendMessage(new SendMessage(String.valueOf(chatId), "Ваша статистика: \n" + stat));
                sendMenu(chatId);
            }
            case "/info" -> {
                sendMessage(new SendMessage(String.valueOf(chatId), gameConfig.getInfo()));
                sendMenu(chatId);
            }
            case "/option1",
                 "/option2",
                 "/option3",
                 "/option4" -> {
                boolean isPositiveAnswer = handleAnswer(chatId, session, callbackData);
                if (isPositiveAnswer) {
                    if (session.getQuestions().isEmpty()) {
                        finishGame(session, chatId);
                        sendMenu(chatId);
                    } else {
                        sessionService.setCurrentQuestion(session);
                        showAnswerOptions(session, chatId);
                    }
                } else {
                    finishGame(session, chatId);
                    sendMenu(chatId);
                }
            }
            case "/help1",
                 "/help2",
                 "/help3" -> {
                handleHelp(chatId, session, callbackData);
                showAnswerOptions(session, chatId);
            }
            default -> throw new UnsupportedOperationException("Неизвестная команда - " + callbackData);
        }
        sendMessage(new AnswerCallbackQuery(callbackQueryId));
    }

    private void sendGreeting(Long chatId, String userName) {
        sendMessage(new SendMessage(String.valueOf(chatId), "Привет, " + userName + "!"));
    }

    private void showAnswerOptions(Session session, Long chatId) {
        Question currentQuestion = session.getCurrentQuestion();
        if (currentQuestion == null) {
            return;
        }

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(session.getCurrentQuestion().getQuestion());
        message.setReplyMarkup(keyboardService.answerMenu(session));

        sendMessage(message);
    }

    private void finishGame(Session session, Long chatId) {
        Integer score = profileService.addAndGetTotalScore(session);

        int correctAnswerCount;
        if (session.getQuestions().isEmpty()) {
            correctAnswerCount = WIN_LEVEL;
        } else {
            correctAnswerCount = session.getLevel() - 1;
        }

        profileService.addCorrectAnswers(session.getProfile(), correctAnswerCount);

        String message = "Игра закончилась! \n" +
                         "Вы заработали " + score + " очков \n" +
                         "Правильных ответов - " + correctAnswerCount;

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);

        sendMessage(sendMessage);

        sessionService.clear(session);
    }

    private boolean handleAnswer(Long chatId, Session session, String answer) {
        Question currentQuestion = session.getCurrentQuestion();
        var option = switch (answer) {
            case "/option1" -> currentQuestion.getOption1();
            case "/option2" -> currentQuestion.getOption2();
            case "/option3" -> currentQuestion.getOption3();
            case "/option4" -> currentQuestion.getOption4();
            default -> throw new IllegalStateException("Неизвестный ввод: " + answer);
        };

        profileService.incrementPassedQuestion(session.getProfile());

        if (option.equals(currentQuestion.getAnswer())) {
            positiveResult(chatId, session);
            return true;
        } else {
            negativeResult(chatId, session);
            return false;
        }
    }

    private void negativeResult(Long chatId, Session session) {
        StringBuilder sb = new StringBuilder();
        sb.append("Неверный ответ \uD83D\uDC4E\n");
        sb.append("Правильный - " + session.getCurrentQuestion().getAnswer());

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());

        sendMessage(message);
    }

    private void positiveResult(Long chatId, Session session) {
        Integer currentScore = gameConfig.getScoreTable().get(session.getLevel());

        StringBuilder sb = new StringBuilder();
        sb.append("Правильно \uD83D\uDD25\n");
        sb.append("Вы заработали " + currentScore + " очков\n");
        if (GameConstants.isFireproofLevel(session.getLevel())) {
            sb.append("Достигнута несгораемая сумма!\n");
        }
        sb.append("Следующий вопрос: \n");

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(sb.toString());

        sendMessage(message);

        sessionService.addScore(session);
    }

    private void handleHelp(Long chatId, Session session, String callbackData) {
        switch (callbackData) {
            case "/help1" -> helpService.handleHelp1(session);
            case "/help2" -> {
                String result = helpService.handleHelp2(session);
                sendMessage(new SendMessage(String.valueOf(chatId), result));
            }
            case "/help3" -> {
                String result = helpService.handleHelp3(session);
                sendMessage(new SendMessage(String.valueOf(chatId), result));
            }
            default -> throw new UnsupportedOperationException("Неизвестная команда - " + callbackData);
        }
    }

    private void sendMenu(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери действие:");
        sendMessage.setReplyMarkup(keyboardService.mainMenu());

        sendMessage(sendMessage);
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
