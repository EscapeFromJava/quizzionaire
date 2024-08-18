package com.reske.taptapbot.service;

import com.reske.taptapbot.common.TextConstants;
import com.reske.taptapbot.entity.Question;
import com.reske.taptapbot.model.Helper;
import com.reske.taptapbot.model.Session;
import org.springframework.stereotype.Component;

import static com.reske.taptapbot.util.UtilClass.RANDOM;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

@Component
public class HelpService {

    private final static List<Helper> GARANT_HELPERS = List.of(
            new Helper("Александр Друзь", "Привет! Я совершенно уверен, что правильный ответ - "),
            new Helper("Максим Поташев", "Добрый вечер! Абсолютно точно будет сказать, что правильный ответ - "),
            new Helper("Борис Бурда", "О, здравствуй! Уверенно заявляю, что правильный ответ - ")
    );
    private final static List<Helper> POSSIBLE_HELPERS = List.of(
            new Helper("Дядя Ваня", "Здарова! Не знаю, возможно это - "),
            new Helper("Тетя Маша", "Здравствуй, здравствуй! Мне кажется что это - "),
            new Helper("Одноклассник Боря", "Салам, старина! У тебя же в школе лучше было? Ну попробуй ответить - ")
    );
    private final static List<Helper> NOT_HELPERS = List.of(
            new Helper("Чайник", "Ало! Че? Ты куда звонишь???"),
            new Helper("8 800 555 35 35", "Лучше позвонить, чем у кого-то занимать!"),
            new Helper("ООО МТС", "Уважаемый абонент! У вас заканчивается срок действия договора...")
    );

    public void handleHelp1(Session session) {
        Question currentQuestion = session.getCurrentQuestion();
        String answer = currentQuestion.getAnswer();

        Map<String, Integer> currentQuestionsMap = new HashMap<>();
        currentQuestionsMap.put(currentQuestion.getOption1(), 1);
        currentQuestionsMap.put(currentQuestion.getOption2(), 2);
        currentQuestionsMap.put(currentQuestion.getOption3(), 3);
        currentQuestionsMap.put(currentQuestion.getOption4(), 4);

        Integer correctId = currentQuestionsMap.get(answer);

        currentQuestionsMap.remove(answer);

        List<Integer> possibleOptions = currentQuestionsMap.values().stream().toList();
        Integer incorrectId = possibleOptions.get(RANDOM.nextInt(possibleOptions.size()));

        List<Integer> nullOptions = new ArrayList<>();
        for (int i = 1; i < 5; i++) {
            if (i != correctId && i != incorrectId) {
                nullOptions.add(i);
            }
        }

        for (Integer nullOption : nullOptions) {
            switch (nullOption) {
                case 1 -> currentQuestion.setOption1(null);
                case 2 -> currentQuestion.setOption2(null);
                case 3 -> currentQuestion.setOption3(null);
                case 4 -> currentQuestion.setOption4(null);
            }
        }

        session.setHelp1Used(true);
    }

    public String handleHelp2(Session session) {
        int val1 = RANDOM.nextInt(101);
        int val2;

        List<Integer> collections = new ArrayList<>();

        String result;

        if (isUsedHelp1OnThisLevel(session)) {
            val2 = 100 - val1;

            collections.add(val1);
            collections.add(val2);

            Collections.shuffle(collections);

            Stack<Integer> stack = new Stack<>();
            stack.addAll(collections);

            Question currentQuestion = session.getCurrentQuestion();

            List<Integer> possibleOptions = new ArrayList<>();
            if (currentQuestion.getOption1() != null) {
                possibleOptions.add(1);
            }
            if (currentQuestion.getOption2() != null) {
                possibleOptions.add(2);
            }
            if (currentQuestion.getOption3() != null) {
                possibleOptions.add(3);
            }
            if (currentQuestion.getOption4() != null) {
                possibleOptions.add(4);
            }

            StringBuilder sb = new StringBuilder("Результат голосования:");
            sb.append(TextConstants.LINE_BREAK);
            for (Integer possibleOption : possibleOptions) {
                Integer temp = stack.pop();
                switch (possibleOption) {
                    case 1 -> sb.append("A: ");
                    case 2 -> sb.append("B: ");
                    case 3 -> sb.append("C: ");
                    case 4 -> sb.append("D: ");
                }
                sb.append(temp);
                sb.append(TextConstants.LINE_BREAK);
            }

            result = sb.toString();
        } else {
            val2 = RANDOM.nextInt(101 - val1);
            int val3 = RANDOM.nextInt(101 - val1 - val2);
            int val4 = 100 - val1 - val2 - val3;

            collections.add(val1);
            collections.add(val2);
            collections.add(val3);
            collections.add(val4);

            Collections.shuffle(collections);

            result = "Результат голосования:" + TextConstants.LINE_BREAK +
                     "А: " + collections.get(0) + TextConstants.LINE_BREAK +
                     "B: " + collections.get(1) + TextConstants.LINE_BREAK +
                     "C: " + collections.get(2) + TextConstants.LINE_BREAK +
                     "D: " + collections.get(3) + TextConstants.LINE_BREAK;
        }

        session.setHelp2Used(true);

        return result;
    }

    public String handleHelp3(Session session) {
        Integer level = session.getLevel();
        Helper helper;
        String result;
        Question currentQuestion = session.getCurrentQuestion();
        if (level < 6) {
            helper = getHelperByHintLevel(GARANT_HELPERS);
            result = generateHelperAnswer(helper, List.of(currentQuestion.getAnswer()));
        } else if (level < 11) {
            helper = getHelperByHintLevel(POSSIBLE_HELPERS);
            result = generateHelperAnswer(
                    helper,
                    List.of(
                            currentQuestion.getOption1(),
                            currentQuestion.getOption2(),
                            currentQuestion.getOption3(),
                            currentQuestion.getOption4()));
        } else {
            helper = getHelperByHintLevel(NOT_HELPERS);
            result = generateHelperAnswer(helper, List.of());
        }

        session.setHelp3Used(true);

        return result;
    }

    private boolean isUsedHelp1OnThisLevel(Session session) {
        Question currentQuestion = session.getCurrentQuestion();
        return currentQuestion.getOption1() == null
               || currentQuestion.getOption2() == null
               || currentQuestion.getOption3() == null
               || currentQuestion.getOption4() == null;
    }

    private String generateHelperAnswer(Helper helper, List<String> answers) {
        if (answers.size() == 1) {
            return helper.name() + ": " + helper.phrase() + answers.get(0);
        } else if (answers.size() == 4) {
            List<String> processedAnswers = answers.stream()
                    .filter(Objects::nonNull)
                    .toList();
            String answer = processedAnswers.get(RANDOM.nextInt(processedAnswers.size()));
            return helper.name() + ": " + helper.phrase() + answer;
        } else {
            return helper.name() + ": " + helper.phrase();
        }
    }

    private Helper getHelperByHintLevel(List<Helper> helpers) {
        return helpers.get(RANDOM.nextInt(helpers.size()));
    }

}
