package org.example;

import bot.MySurveyBot;
import gui.CommunityDashboard;
import gui.CustomDialogs;
import models.CommunityMember;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static final String MSG_BOT_SUCCESS = "הבוט פועל בהצלחה!";
    private static final String TITLE_CONN_ERROR = "שגיאת חיבור";
    private static final String MSG_CONN_ERROR = "שגיאה בחיבור לטלגרם:\n";

    public static void main(String[] args) {

        List<CommunityMember> globalCommunity = new ArrayList<>();
        CommunityDashboard dashboard = new CommunityDashboard();
        MySurveyBot myBot = new MySurveyBot(globalCommunity, dashboard);

        dashboard.getSurveyCreatorPanel().setBotAndCommunity(myBot, globalCommunity);

        registerBot(myBot);

        SwingUtilities.invokeLater(() -> dashboard.setVisible(true));
    }

    private static void registerBot(MySurveyBot myBot) {
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(myBot);
            System.out.println(MSG_BOT_SUCCESS);
        } catch (TelegramApiException e) {
            CustomDialogs.showMessage(null, TITLE_CONN_ERROR, MSG_CONN_ERROR + e.getMessage(), true);
        }
    }
}