package bot;

import gui.CustomDialogs;
import gui.CommunityDashboard;
import models.CommunityMember;
import models.Question;
import models.Survey;
import models.SurveyParticipant;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class MySurveyBot extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "8489228069:AAE8OMNqi2IDuvCzwS7xJ_XwdXLc8_Qm6A4";
    private static final String BOT_USERNAME = "TheSurveysBot";
    private static final String CALLBACK_PREFIX_ANSWER = "ANS_";
    private static final String CALLBACK_DATA_DELIMITER = "_";

    private static final int MAX_SURVEY_TIME_SECONDS = 300;
    private static final int REMINDER_TIME_SECONDS = 180;
    private static final int TIMER_DELAY_MS = 1000;

    private static final int PART_INDEX_QUESTION = 1;
    private static final int PART_INDEX_OPTION = 2;
    private static final int FIRST_QUESTION_INDEX = 0;
    private static final int TIME_LEFT_ZERO = 0;

    private static final String CMD_START = "/start";
    private static final String CMD_START_DESC = "הצטרפות לקהילת הסקרים";
    private static final String CMD_HI_HEB = "היי";
    private static final String CMD_HI_ENG = "hi";

    private static final String PROGRESS_ICON_DONE = "🟩";
    private static final String PROGRESS_ICON_PENDING = "⬜";

    private static final String MSG_ALREADY_REGISTERED = "היי %s, אתה כבר רשום בקהילה שלנו! 📋\nאנא המתן בסבלנות לסקר הבא שיישלח בקרוב.";
    private static final String MSG_WELCOME = "ברוך הבא לקהילת הסקרים שלנו, %s! 📊\nברגע שיופעל סקר חדש, אתה תקבל אותו ישירות לכאן.";
    private static final String MSG_BROADCAST_NEW = "📣 עדכון: %s הצטרף/ה לקהילה!\nכעת אנחנו %d חברים.";

    private static final String MSG_SURVEY_START = "🎉 סקר חדש בנושא '%s' מתחיל עכשיו!\n⏱️ שימו לב: יש לכם בדיוק 5 דקות להשלים את כל השאלות. בהצלחה!";
    private static final String MSG_QUESTION_FORMAT = "%s %d/%d\nשאלה %d:\n\n%s";
    private static final String MSG_ALREADY_ANSWERED = "כבר ענית על שאלה זו! 🚫";
    private static final String MSG_ANSWER_SAVED = "תשובתך נקלטה בהצלחה! ✅";
    private static final String MSG_SURVEY_COMPLETED = "תודה רבה על השתתפותך! סיימת את הסקר בהצלחה. 🎉";

    private static final String MSG_REMINDER = "⏰ תזכורת: יש סקר פעיל שטרם סיימת! הזדרז, הסקר ייסגר בעוד 2 דקות.";
    private static final String MSG_TIMEOUT = "הזמן להשיב על הסקר תם! 🕒 נשמח מאוד לשמוע את דעתך בסקרים הבאים שלנו. המשך יום מקסים! 😊";

    private static final String MSG_SURVEY_INACTIVE = "הסקר אינו פעיל כרגע.";
    private static final String MSG_ERROR_PROCESS = "שגיאה בעיבוד התשובה. אנא נסה שוב.";
    private static final String MSG_REASON_EARLY = "הסקר נסגר מוקדם מהצפוי מכיוון שכל המשתתפים השלימו אותו!";
    private static final String MSG_REASON_TIMEOUT = "הזמן המוקצב לסקר (5 דקות) תם!";

    private static final String DIALOG_CLOSE_TITLE = "סיום סקר";
    private static final String DIALOG_CLOSE_BODY = "הסקר נסגר!\n%s";

    private final List<CommunityMember> globalCommunity;
    private final CommunityDashboard dashboard;

    private Survey activeSurvey;
    private boolean isSurveyActive = false;
    private Timer activeSurveyTimer;
    private int secondsElapsed;

    public MySurveyBot(List<CommunityMember> globalCommunity, CommunityDashboard dashboard) {
        this.globalCommunity = globalCommunity;
        this.dashboard = dashboard;
        setupBotMenu();
    }

    private void setupBotMenu() {
        List<BotCommand> commands = new ArrayList<>();
        commands.add(new BotCommand(CMD_START, CMD_START_DESC));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (Exception e) {
            System.err.println("שגיאה ביצירת תפריט בוט: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    public boolean isSurveyActive() {
        return this.isSurveyActive;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            handleTextMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        }
    }

    private void handleTextMessage(Message message) {
        String messageText = message.getText();
        long chatId = message.getChatId();
        String firstName = message.getFrom().getFirstName();
        String username = message.getFrom().getUserName();

        if (messageText.equals(CMD_START) || messageText.equalsIgnoreCase(CMD_HI_HEB) || messageText.equalsIgnoreCase(CMD_HI_ENG)) {
            handleNewUser(chatId, firstName, username);
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String callData = callbackQuery.getData();
        long chatId = callbackQuery.getMessage().getChatId();
        String queryId = callbackQuery.getId();

        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(queryId);

        if (activeSurvey == null || !isSurveyActive) {
            answer.setText(MSG_SURVEY_INACTIVE);
            sendTelegramAction(answer);
            return;
        }

        if (callData.startsWith(CALLBACK_PREFIX_ANSWER)) {
            processSurveyAnswer(callData, chatId, answer);
        }
    }

    private void processSurveyAnswer(String callData, long chatId, AnswerCallbackQuery answer) {
        try {
            String[] parts = callData.split(CALLBACK_DATA_DELIMITER);
            int currentQuestionIndex = Integer.parseInt(parts[PART_INDEX_QUESTION]);
            int selectedOptionIndex = Integer.parseInt(parts[PART_INDEX_OPTION]);

            boolean alreadyAnswered = false;

            for (SurveyParticipant participant : activeSurvey.getParticipants()) {
                if (participant.getMember().getChatId() == chatId) {
                    if (participant.getAnswers().size() > currentQuestionIndex) {
                        alreadyAnswered = true;
                    } else {
                        participant.addAnswer(selectedOptionIndex);
                    }
                    break;
                }
            }

            if (alreadyAnswered) {
                answer.setText(MSG_ALREADY_ANSWERED);
                sendTelegramAction(answer);
                return;
            } else {
                answer.setText(MSG_ANSWER_SAVED);
                sendTelegramAction(answer);
            }

            dashboard.getActiveSurveyPanel().updateSurveyStatus(activeSurvey);

            int nextQuestionIndex = currentQuestionIndex + 1;
            if (nextQuestionIndex < activeSurvey.getQuestions().size()) {
                sendQuestionWithOptions(chatId, activeSurvey.getQuestionByIndex(nextQuestionIndex), nextQuestionIndex);
            } else {
                sendMessage(chatId, MSG_SURVEY_COMPLETED);
            }

            checkIfAllFinished();

        } catch (Exception e) {
            answer.setText(MSG_ERROR_PROCESS);
            sendTelegramAction(answer);
        }
    }

    private void sendTelegramAction(AnswerCallbackQuery answer) {
        try {
            execute(answer);
        } catch (TelegramApiException e) {
            System.err.println("שגיאה בשליחת תגובת כפתור: " + e.getMessage());
        }
    }

    private void checkIfAllFinished() {
        if (!isSurveyActive) return;

        int totalQuestions = activeSurvey.getQuestions().size();
        boolean everyoneDone = true;

        for (SurveyParticipant p : activeSurvey.getParticipants()) {
            if (p.getAnswers().size() < totalQuestions) {
                everyoneDone = false;
                break;
            }
        }

        if (everyoneDone) {
            closeSurvey(MSG_REASON_EARLY);
        }
    }

    private void closeSurvey(String reason) {
        if (!isSurveyActive) return;

        isSurveyActive = false;

        if (activeSurveyTimer != null) {
            activeSurveyTimer.stop();
        }

        dashboard.getActiveSurveyPanel().updateTimeRemaining(TIME_LEFT_ZERO);
        CustomDialogs.showMessage(dashboard, DIALOG_CLOSE_TITLE, String.format(DIALOG_CLOSE_BODY, reason), false);
        dashboard.showSurveyResults(activeSurvey);
    }

    private void notifyIncompleteParticipants() {
        if (activeSurvey == null) return;
        int totalQuestions = activeSurvey.getQuestions().size();

        for (SurveyParticipant p : activeSurvey.getParticipants()) {
            if (p.getAnswers().size() < totalQuestions) {
                sendMessage(p.getMember().getChatId(), MSG_TIMEOUT);
            }
        }
    }

    private void sendReminders() {
        int totalQuestions = activeSurvey.getQuestions().size();
        for (SurveyParticipant p : activeSurvey.getParticipants()) {
            if (p.getAnswers().size() < totalQuestions) {
                sendMessage(p.getMember().getChatId(), MSG_REMINDER);
            }
        }
    }

    private void handleNewUser(long chatId, String firstName, String username) {
        for (CommunityMember member : globalCommunity) {
            if (member.getChatId() == chatId) {
                sendMessage(chatId, String.format(MSG_ALREADY_REGISTERED, firstName));
                return;
            }
        }

        CommunityMember newMember = new CommunityMember(chatId, firstName, username);
        globalCommunity.add(newMember);

        sendMessage(chatId, String.format(MSG_WELCOME, firstName));
        broadcastNewMember(chatId, firstName);
        dashboard.addMemberToUI(newMember);
    }

    private void broadcastNewMember(long newMemberChatId, String newMemberName) {
        String text = String.format(MSG_BROADCAST_NEW, newMemberName, globalCommunity.size());
        for (CommunityMember member : globalCommunity) {
            if (member.getChatId() != newMemberChatId) {
                sendMessage(member.getChatId(), text);
            }
        }
    }

    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("שגיאה בשליחת הודעה: " + e.getMessage());
        }
    }

    private String generateProgressBar(int currentIndex, int totalQuestions) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < totalQuestions; i++) {
            if (i <= currentIndex) {
                sb.append(PROGRESS_ICON_DONE);
            } else {
                sb.append(PROGRESS_ICON_PENDING);
            }
        }
        return sb.toString();
    }

    public void sendQuestionWithOptions(long chatId, Question question, int questionIndex) {
        int totalQuestions = activeSurvey.getQuestions().size();
        String progressBar = generateProgressBar(questionIndex, totalQuestions);
        String questionHeader = String.format(MSG_QUESTION_FORMAT, progressBar, (questionIndex + 1), totalQuestions, (questionIndex + 1), question.getText());

        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(questionHeader);

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(options.get(i));
            button.setCallbackData(CALLBACK_PREFIX_ANSWER + questionIndex + CALLBACK_DATA_DELIMITER + i);
            rowInline.add(button);
            rowsInline.add(rowInline);
        }
        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("שגיאה בשליחת שאלה: " + e.getMessage());
        }
    }

    public void startSurvey(Survey newSurvey) {
        this.activeSurvey = newSurvey;
        this.isSurveyActive = true;
        this.secondsElapsed = 0;

        dashboard.getActiveSurveyPanel().updateSurveyStatus(activeSurvey);

        String introMessage = String.format(MSG_SURVEY_START, newSurvey.getTopic());

        for (SurveyParticipant p : activeSurvey.getParticipants()) {
            long chatId = p.getMember().getChatId();
            sendMessage(chatId, introMessage);
            sendQuestionWithOptions(chatId, activeSurvey.getQuestionByIndex(FIRST_QUESTION_INDEX), FIRST_QUESTION_INDEX);
        }

        if (activeSurveyTimer != null) {
            activeSurveyTimer.stop();
        }

        activeSurveyTimer = new Timer(TIMER_DELAY_MS, e -> {
            secondsElapsed++;
            int secondsLeft = MAX_SURVEY_TIME_SECONDS - secondsElapsed;

            dashboard.getActiveSurveyPanel().updateTimeRemaining(secondsLeft);

            if (secondsElapsed == REMINDER_TIME_SECONDS) {
                sendReminders();
            }

            if (secondsLeft <= 0) {
                notifyIncompleteParticipants();
                closeSurvey(MSG_REASON_TIMEOUT);
            }
        });

        activeSurveyTimer.start();
    }
}