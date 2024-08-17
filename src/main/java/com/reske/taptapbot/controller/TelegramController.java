package com.reske.taptapbot.controller;

import com.reske.taptapbot.config.BotProperties;
import com.reske.taptapbot.entity.Profile;
import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.model.Session;
import com.reske.taptapbot.service.ProfileService;
import com.reske.taptapbot.service.QuestionService;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramController extends TelegramLongPollingBot {

    private final BotProperties botProperties;
    private final SessionService sessionService;
    private final ProfileService profileService;
    private final QuestionService questionService;

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String userName = update.getMessage().getChat().getUserName();

            Profile profile = profileService.getOrCreate(chatId, userName);

            Session session = new Session(profile);
            sessionService.addProfile(session);

            sendGreeting(chatId, userName);
            sendMenu(chatId);
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            String callbackQueryId = update.getCallbackQuery().getId();
            Long chatId = update.getCallbackQuery().getMessage().getChatId();

            Session session = sessionService.get(chatId);

            switch (callbackData) {
                case "/play" -> {
                    sessionService.addQuestions(session);
                    step(session, chatId);
                }
                case "/stat" -> {
                    String stat = profileService.getStat(session.getProfile());
                    sendMessage(new SendMessage(String.valueOf(chatId), "Ваша статистика: \n" + stat));
                    sendMenu(chatId);
                }
                case "/info" -> {
                    String info = """
                            Отвечайте на вопросы и зарабатывайте очки!
                            Всего 15 вопросов.
                            При правильном ответе на каждый 5-ый вопрос фиксируется несгораемая сумма очков.
                            Таблица стоимости каждого вопроса:
                            1 - 100
                            2 - 200
                            3 - 300
                            4 - 500
                            5 - 1000 - несгораемая сумма
                            6 - 2000
                            7 - 4000
                            8 - 8000
                            9 - 16000
                            10 - 32000 - несгораемая сумма
                            11 - 64000
                            12 - 125000
                            13 - 250000
                            14 - 500000
                            15 - 1000000
                            У вас есть 3 подсказки:
                            50/50 - исключает два неправильных ответа
                            Помощь зала - имитация ответа аудитории
                            Звонок другу - имитация звонка другу
                            Удачи! \n
                            """;
                    sendMessage(new SendMessage(String.valueOf(chatId), info));
                    sendMenu(chatId);
                }
                default -> {
                    handleAnswer(chatId, session, callbackData);
                    step(session, chatId);
                }
            }
            sendMessage(new AnswerCallbackQuery(callbackQueryId));
        }
    }

    private void sendGreeting(Long chatId, String userName) {
        sendMessage(new SendMessage(String.valueOf(chatId), "Привет, " + userName + "!"));
    }

    private void step(Session session, Long chatId) {
        Question currentQuestion = questionService.getRandomQuestion(session);

        if (currentQuestion == null) {
            finishGame(session, chatId);
            sendMenu(chatId);
            return;
        }

        InlineKeyboardButton option1 = new InlineKeyboardButton();
        option1.setText(currentQuestion.getOption1());
        option1.setCallbackData("/option1");
        InlineKeyboardButton option2 = new InlineKeyboardButton();
        option2.setText(currentQuestion.getOption2());
        option2.setCallbackData("/option2");
        InlineKeyboardButton option3 = new InlineKeyboardButton();
        option3.setText(currentQuestion.getOption3());
        option3.setCallbackData("/option3");
        InlineKeyboardButton option4 = new InlineKeyboardButton();
        option4.setText(currentQuestion.getOption4());
        option4.setCallbackData("/option4");

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        row1.add(option1);
        row1.add(option2);

        List<InlineKeyboardButton> row2 = new ArrayList<>();
        row2.add(option3);
        row2.add(option4);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(session.getCurrentQuestion().getQuestion());
        message.setReplyMarkup(keyboardMarkup);

        sendMessage(message);
    }

    private void finishGame(Session session, Long chatId) {
        profileService.addScore(session);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        String message = "Игра закончилась! \n" +
                         "Вы заработали " + session.getScore() + " очков \n" +
                         "Правильных ответов - " + session.getLevel();
        sendMessage(new SendMessage(String.valueOf(chatId), message));

        sessionService.clear(session);
    }

    private void handleAnswer(Long chatId, Session session, String answer) {
        Question currentQuestion = session.getCurrentQuestion();
        var option = switch (answer) {
            case "/option1" -> currentQuestion.getOption1();
            case "/option2" -> currentQuestion.getOption2();
            case "/option3" -> currentQuestion.getOption3();
            case "/option4" -> currentQuestion.getOption4();
            default -> throw new IllegalStateException("Неизвестный ввод: " + answer);
        };
        if (option.equals(currentQuestion.getAnswer())) {
            sendMessage(new SendMessage(String.valueOf(chatId), "Правильно \uD83D\uDD25"));
            profileService.incrementCorrectAnswers(session.getProfile());
            sessionService.addScore(session);
        } else {
            sendMessage(new SendMessage(String.valueOf(chatId), "Неверный ответ \uD83D\uDC4E. Правильный - " + currentQuestion.getAnswer()));
        }
        profileService.addPassedQuestion(session);
    }

    private void sendMenu(Long chatId) {
        InlineKeyboardButton play = new InlineKeyboardButton();
        play.setText("Играть");
        play.setCallbackData("/play");

        InlineKeyboardButton stat = new InlineKeyboardButton();
        stat.setText("Статистика");
        stat.setCallbackData("/stat");

        InlineKeyboardButton info = new InlineKeyboardButton();
        info.setText("Информация");
        info.setCallbackData("/info");

        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(play);
        row.add(stat);
        row.add(info);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Выбери действие:");
        sendMessage.setReplyMarkup(keyboardMarkup);

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
