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

import java.awt.Toolkit;
import javax.swing.Timer;
import java.util.ArrayList;
import java.util.List;

public class MySurveyBot extends TelegramLongPollingBot {

    private static final String BOT_TOKEN = "8489228069:AAE8OMNqi2IDuvCzwS7xJ_XwdXLc8_Qm6A4";
    private static final String BOT_USERNAME = "TheSurveysBot";
    private static final String CALLBACK_PREFIX_ANSWER = "ANS_";

    private static final int MAX_SURVEY_TIME_SECONDS = 300;
    private static final int REMINDER_TIME_SECONDS = 180;
    private static final int TIMER_DELAY_MS = 1000;

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
        commands.add(new BotCommand("/start", "הצטרפות לקהילת הסקרים"));

        try {
            this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
        } catch (Exception e) {
            System.err.println("שגיאה ביצירת תפריט בוט: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() { return BOT_USERNAME; }

    @Override
    public String getBotToken() { return BOT_TOKEN; }

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

        if (messageText.equals("/start") || messageText.equalsIgnoreCase("היי") || messageText.equalsIgnoreCase("hi")) {
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
            answer.setText("הסקר אינו פעיל כרגע.");
            sendTelegramAction(answer);
            return;
        }

        if (callData.startsWith(CALLBACK_PREFIX_ANSWER)) {
            processSurveyAnswer(callData, chatId, answer);
        }
    }

    private void processSurveyAnswer(String callData, long chatId, AnswerCallbackQuery answer) {
        try {
            String[] parts = callData.split("_");
            int currentQuestionIndex = Integer.parseInt(parts[1]);
            int selectedOptionIndex = Integer.parseInt(parts[2]);

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
                answer.setText("כבר ענית על שאלה זו! 🚫");
                sendTelegramAction(answer);
                return;
            } else {
                sendTelegramAction(answer);
            }

            dashboard.getActiveSurveyPanel().updateSurveyStatus(activeSurvey);

            int nextQuestionIndex = currentQuestionIndex + 1;
            if (nextQuestionIndex < activeSurvey.getQuestions().size()) {
                sendQuestionWithOptions(chatId, activeSurvey.getQuestionByIndex(nextQuestionIndex), nextQuestionIndex);
            } else {
                sendMessage(chatId, "תודה רבה על השתתפותך! סיימת את הסקר בהצלחה. 🎉");
            }

            checkIfAllFinished();

        } catch (Exception e) {
            answer.setText("שגיאה בעיבוד התשובה. אנא נסה שוב.");
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
            closeSurvey("הסקר נסגר מוקדם מהצפוי מכיוון שכל המשתתפים השלימו אותו!");
        }
    }

    private void closeSurvey(String reason) {
        if (!isSurveyActive) return;

        Toolkit.getDefaultToolkit().beep();
        isSurveyActive = false;

        if (activeSurveyTimer != null) {
            activeSurveyTimer.stop();
        }

        dashboard.getActiveSurveyPanel().updateTimeRemaining(0);
        CustomDialogs.showMessage(dashboard, "סיום סקר", "הסקר נסגר!\n" + reason, false);
        dashboard.showSurveyResults(activeSurvey);
    }

    private void sendReminders() {
        int totalQuestions = activeSurvey.getQuestions().size();
        for (SurveyParticipant p : activeSurvey.getParticipants()) {
            if (p.getAnswers().size() < totalQuestions) {
                sendMessage(p.getMember().getChatId(), "⏰ תזכורת: יש סקר פעיל שטרם סיימת! הזדרז, הסקר ייסגר בעוד 2 דקות.");
            }
        }
    }

    private void handleNewUser(long chatId, String firstName, String username) {
        for (CommunityMember member : globalCommunity) {
            if (member.getChatId() == chatId) {
                sendMessage(chatId, "היי " + firstName + ", אתה כבר רשום בקהילה שלנו! 📋\nאנא המתן בסבלנות לסקר הבא שיישלח בקרוב.");
                return;
            }
        }

        CommunityMember newMember = new CommunityMember(chatId, firstName, username);
        globalCommunity.add(newMember);

        String welcomeMessage = "ברוך הבא לקהילת הסקרים שלנו, " + firstName + "! 📊\n" +
                "ברגע שיופעל סקר חדש, אתה תקבל אותו ישירות לכאן.";
        sendMessage(chatId, welcomeMessage);
        broadcastNewMember(chatId, firstName);
        dashboard.addMemberToUI(newMember);
    }

    private void broadcastNewMember(long newMemberChatId, String newMemberName) {
        String text = "📣 עדכון: " + newMemberName + " הצטרף/ה לקהילה!\nכעת אנחנו " + globalCommunity.size() + " חברים.";
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

    public void sendQuestionWithOptions(long chatId, Question question, int questionIndex) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("שאלה " + (questionIndex + 1) + ":\n" + question.getText());

        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        List<String> options = question.getOptions();
        for (int i = 0; i < options.size(); i++) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(options.get(i));
            button.setCallbackData(CALLBACK_PREFIX_ANSWER + questionIndex + "_" + i);
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

        String introMessage = "🎉 סקר חדש בנושא '" + newSurvey.getTopic() + "' מתחיל עכשיו!\n" +
                "⏱️ שימו לב: יש לכם בדיוק 5 דקות להשלים את כל השאלות. בהצלחה!";

        for (SurveyParticipant p : activeSurvey.getParticipants()) {
            long chatId = p.getMember().getChatId();
            sendMessage(chatId, introMessage);
            sendQuestionWithOptions(chatId, activeSurvey.getQuestionByIndex(0), 0);
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
                closeSurvey("הזמן המוקצב לסקר (5 דקות) תם!");
            }
        });

        activeSurveyTimer.start();
    }
}