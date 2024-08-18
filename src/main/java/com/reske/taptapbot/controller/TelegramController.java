package com.reske.taptapbot.controller;

import com.reske.taptapbot.config.BotProperties;
import com.reske.taptapbot.entity.Profile;
import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.model.Session;
import com.reske.taptapbot.service.HelpService;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramController extends TelegramLongPollingBot {

    private static final Random RANDOM = new Random();

    private final BotProperties botProperties;
    private final SessionService sessionService;
    private final ProfileService profileService;
    private final HelpService helpService;

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
                    setQuestion(session, chatId);
                    showAnswerOptions(session, chatId);
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
                case "/option1",
                     "/option2",
                     "/option3",
                     "/option4" -> {
                    boolean endGame = handleAnswer(chatId, session, callbackData);
                    if (endGame) {
                        finishGame(session, chatId);
                        sendMenu(chatId);
                    } else {
                        setQuestion(session, chatId);
                        showAnswerOptions(session, chatId);
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
    }

    private void sendGreeting(Long chatId, String userName) {
        sendMessage(new SendMessage(String.valueOf(chatId), "Привет, " + userName + "!"));
    }

    private void showAnswerOptions(Session session, Long chatId) {
        Question currentQuestion = session.getCurrentQuestion();
        if (currentQuestion == null) {
            return;
        }

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        if (currentQuestion.getOption1() != null) {
            InlineKeyboardButton option1 = new InlineKeyboardButton();
            option1.setText("A. " + currentQuestion.getOption1());
            option1.setCallbackData("/option1");
            row1.add(option1);
        }
        if (currentQuestion.getOption2() != null) {
            InlineKeyboardButton option2 = new InlineKeyboardButton();
            option2.setText("B. " + currentQuestion.getOption2());
            option2.setCallbackData("/option2");
            row1.add(option2);
        }
        if (currentQuestion.getOption3() != null) {
            InlineKeyboardButton option3 = new InlineKeyboardButton();
            option3.setText("C. " + currentQuestion.getOption3());
            option3.setCallbackData("/option3");
            row2.add(option3);
        }
        if (currentQuestion.getOption4() != null) {
            InlineKeyboardButton option4 = new InlineKeyboardButton();
            option4.setText("D. " + currentQuestion.getOption4());
            option4.setCallbackData("/option4");
            row2.add(option4);
        }

        List<InlineKeyboardButton> row3 = new ArrayList<>();
        if (!session.isHelp1Used()) {
            InlineKeyboardButton help1 = new InlineKeyboardButton();
            help1.setText("50/50");
            help1.setCallbackData("/help1");
            row3.add(help1);
        }
        if (!session.isHelp2Used()) {
            InlineKeyboardButton help2 = new InlineKeyboardButton();
            help2.setText("Помощь зала");
            help2.setCallbackData("/help2");
            row3.add(help2);
        }
        if (!session.isHelp3Used()) {
            InlineKeyboardButton help3 = new InlineKeyboardButton();
            help3.setText("Звонок другу");
            help3.setCallbackData("/help3");
            row3.add(help3);
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        rows.add(row1);
        rows.add(row2);
        rows.add(row3);

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        keyboardMarkup.setKeyboard(rows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(session.getCurrentQuestion().getQuestion());
        message.setReplyMarkup(keyboardMarkup);

        sendMessage(message);
    }

    private void setQuestion(Session session, Long chatId) {
        List<Question> questions = session.getQuestions();

        if (questions.isEmpty()) {
            finishGame(session, chatId);
            sendMenu(chatId);
            return;
        }

        Question currentQuestion = questions.get(RANDOM.nextInt(questions.size()));
        session.setCurrentQuestion(currentQuestion);
        session.setLevel(session.getLevel() + 1);
        questions.remove(currentQuestion);
    }

    private void finishGame(Session session, Long chatId) {
        Integer score = profileService.addAndGetTotalScore(session);

        int correctAnswerCount;
        if (session.getQuestions().isEmpty()) {
            correctAnswerCount = 15;
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
            sendMessage(new SendMessage(String.valueOf(chatId), "Правильно \uD83D\uDD25"));
            sessionService.addScore(session);
            return false;
        } else {
            sendMessage(new SendMessage(String.valueOf(chatId), "Неверный ответ \uD83D\uDC4E. Правильный - " + currentQuestion.getAnswer()));
            return true;
        }
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
