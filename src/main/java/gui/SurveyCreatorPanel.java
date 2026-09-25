package gui;

import bot.MySurveyBot;
import models.CommunityMember;
import models.Question;
import models.Survey;
import services.AIGeneratorService;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class SurveyCreatorPanel extends JPanel {

    private static final String FONT_FAMILY = "Arial";

    private static final int MIN_COMMUNITY_SIZE = 3;
    private static final int MAX_DELAY_MINUTES = 1440;

    private static final int TIMER_DELAY_AI_PROGRESS = 150;
    private static final int TIMER_DELAY_AI_SUCCESS = 1500;
    private static final int TIMER_DELAY_AI_ERROR = 2000;
    private static final int TIMER_DELAY_COUNTDOWN = 1000;

    private static final int FONT_SIZE_BOLD_14 = 14;
    private static final int FONT_SIZE_PLAIN_16 = 16;
    private static final Font FONT_BOLD_14 = new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_BOLD_14);
    private static final Font FONT_PLAIN_16 = new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_PLAIN_16);

    private static final int BORDER_PADDING_MAIN = 15;
    private static final int BORDER_PADDING_PANEL = 10;
    private static final int LAYOUT_GAP_SMALL = 5;
    private static final int LAYOUT_GAP_MEDIUM = 10;
    private static final int LAYOUT_GAP_LARGE = 15;

    private static final int TOPIC_FIELD_COLUMNS = 25;
    private static final int DELAY_FIELD_COLUMNS = 3;
    private static final int MANUAL_OPTION_COLUMNS = 20;
    private static final int PROGRESS_BAR_MIN = 0;
    private static final int PROGRESS_BAR_MAX = 100;
    private static final int MANUAL_PANELS_COUNT = 3;
    private static final int GRID_OPTIONS_ROWS = 4;
    private static final int GRID_OPTIONS_COLS = 1;

    private static final int SECONDS_IN_MINUTE = 60;
    private static final int MILLIS_IN_SECOND = 1000;
    private static final int PROGRESS_MOCK_INCREMENT = 1;
    private static final int PROGRESS_MOCK_STAGE_1 = 30;
    private static final int PROGRESS_MOCK_STAGE_2 = 60;
    private static final int PROGRESS_MOCK_MAX_LIMIT = 90;
    private static final int TAB_INDEX_AI = 1;
    private static final int MIN_MANDATORY_OPTIONS = 2;
    private static final Integer[] Q_COUNT_OPTIONS = {1, 2, 3};
    private static final Integer[] OPT_COUNT_OPTIONS = {2, 3, 4};

    private static final Color COLOR_BTN_DEFAULT = new Color(220, 220, 220);
    private static final Color COLOR_BTN_HOVER_SEND = new Color(173, 216, 230);
    private static final Color COLOR_BTN_HOVER_AI = new Color(144, 238, 144);
    private static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private static final Color COLOR_ERROR = new Color(220, 53, 69);
    private static final Color COLOR_PRIMARY = new Color(70, 130, 180);

    private static final String LABEL_TOPIC = "נושא הסקר הכללי:";
    private static final String LABEL_DELAY = "השהיית שליחה (בדקות, 0 למיידי):";
    private static final String TAB_MANUAL = "יצירה ידנית";
    private static final String TAB_AI = "יצירה באמצעות AI";
    private static final String BTN_SEND_TEXT = "שגר סקר לקהילה";
    private static final String BTN_AI_TEXT = "צור שאלות אוטומטית לפי הנושא";

    private static final String AI_LABEL_Q_COUNT = "כמות שאלות (1-3):";
    private static final String AI_LABEL_OPT_COUNT = "אפשרויות לתשובה (2-4):";
    private static final String AI_RESULTS_TITLE = "השאלות שנוצרו (יוצגו כאן לפני השילוח):";

    private static final String MSG_AI_CONNECTING = "מתחבר לשרתי ChatGPT...";
    private static final String MSG_AI_WRITING = "כותב שאלות...";
    private static final String MSG_AI_FORMATTING = "מנסח תשובות מדויקות...";
    private static final String MSG_AI_SUCCESS = "הושלם בהצלחה!";
    private static final String MSG_AI_FAILED = "שגיאה ביצירה!";
    private static final String MSG_AI_SUCCESS_BODY = "הסקר נוצר בהצלחה!\n\n";

    private static final String ERROR_SYSTEM_BUSY_TITLE = "מערכת עסוקה";
    private static final String ERROR_TITLE = "שגיאה";
    private static final String SUCCESS_TITLE = "הצלחה";
    private static final String CONFIRM_TITLE = "אישור שילוח סקר";

    private static final String ERR_NO_TOPIC_AI = "אנא הזן נושא לסקר בתיבה העליונה.";
    private static final String ERR_NO_TOPIC = "יש להזין נושא כללי לסקר בראש המסך.";
    private static final String ERR_AI_BUSY = "המערכת מייצרת כעת שאלות AI. אנא המתן לסיום התהליך.";
    private static final String ERR_ACTIVE_SURVEY = "שגיאה: קיים סקר פעיל כרגע.\nניתן לנהל סקר אחד בלבד בכל פעם.";
    private static final String ERR_COUNTDOWN_ACTIVE = "שגיאה: קיים סקר שממתין לשילוח (ספירה לאחור רצה).\nלא ניתן להוסיף סקר חדש.";
    private static final String ERR_MIN_COMMUNITY = "שגיאה: דרוש לפחות %d חברי קהילה כדי להתחיל סקר.";
    private static final String ERR_AI_NOT_CREATED = "יש ליצור שאלות AI לפני השילוח.";
    private static final String ERR_MANUAL_FILL = "שגיאה במילוי ידני";
    private static final String ERR_NO_VALID_Q = "יש למלא לפחות שאלה אחת תקינה.";
    private static final String ERR_INVALID_DELAY = "הזן מספר דקות תקין להשהייה (בין 0 ל-%d).";

    private static final String ERR_Q_NO_TEXT = "בשאלה %d חסר נוסח השאלה.";
    private static final String ERR_Q_DUPLICATE = "בשאלה %d יש כפילות: התשובה '%s' מופיעה פעמיים. נא לתקן.";
    private static final String ERR_Q_MIN_OPTS = "בשאלה %d חובה להזין לפחות 2 אפשרויות תשובה.";
    private static final String ERR_Q_MAX_OPTS = "בשאלה %d ניתן להזין עד 4 אפשרויות בלבד.";

    private static final String MSG_CONFIRM_SEND = "האם אתה בטוח שברצונך לשגר את הסקר בנושא:\n'%s'\nל-%d משתתפים?";
    private static final String MSG_SUCCESS_SEND = "הסקר נשלח בהצלחה ל-%d משתתפים!";
    private static final String MSG_COUNTDOWN_RUNNING = "הסקר יישלח בעוד: %02d:%02d";
    private static final String MSG_COUNTDOWN_DONE = "הסקר נשלח!";

    private static final String LBL_MANUAL_Q_TITLE = "שאלה %d";
    private static final String LBL_MANUAL_INCLUDE = "כלול שאלה זו";
    private static final String LBL_MANUAL_Q_TEXT = "נוסח השאלה:";
    private static final String LBL_MANUAL_OPT_MANDATORY = "אפשרות %d (חובה):";
    private static final String LBL_MANUAL_OPT_OPTIONAL = "אפשרות %d (רשות):";

    private static final String PROMPT_TEMPLATE =
            "נושא הסקר: %s\n" +
                    "הוראות קריטיות:\n" +
                    "1. עליך ליצור בדיוק %d שאלות בנושא זה.\n" +
                    "2. עבור כל שאלה, חובה לספק בדיוק %d אפשרויות תשובה. לא פחות מ-%d ולא יותר מ-%d אפשרויות לכל שאלה!\n" +
                    "הקפד על המבנה במדויק.";

    private final JTextField topicField;
    private final JTabbedPane creationMethodTabs;
    private final JTextArea aiResultArea;
    private final JButton generateAiBtn;
    private final JProgressBar aiLoadingBar;
    private final JComboBox<Integer> questionsCountCombo;
    private final JComboBox<Integer> optionsCountCombo;
    private final JTextField delayField;
    private final JLabel countdownLabel;
    private final JButton sendSurveyBtn;
    private final ManualQuestionPanel[] manualQuestionPanels;

    private Timer aiProgressTimer;
    private List<Question> generatedQuestions;
    private final AIGeneratorService aiService;
    private MySurveyBot myBot;
    private List<CommunityMember> globalCommunity;

    private boolean isCountdownActive = false;
    private boolean isAiGenerating = false;

    public SurveyCreatorPanel() {
        aiService = new AIGeneratorService();
        setLayout(new BorderLayout(LAYOUT_GAP_MEDIUM, LAYOUT_GAP_MEDIUM));
        setBorder(BorderFactory.createEmptyBorder(BORDER_PADDING_MAIN, BORDER_PADDING_MAIN, BORDER_PADDING_MAIN, BORDER_PADDING_MAIN));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel topicPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topicPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        topicPanel.add(new JLabel(LABEL_TOPIC));

        topicField = new JTextField(TOPIC_FIELD_COLUMNS);
        topicField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        topicField.setFont(FONT_BOLD_14);
        topicPanel.add(topicField);
        add(topicPanel, BorderLayout.NORTH);

        creationMethodTabs = new JTabbedPane();
        creationMethodTabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        creationMethodTabs.setFont(FONT_BOLD_14);

        manualQuestionPanels = new ManualQuestionPanel[MANUAL_PANELS_COUNT];
        JPanel manualPanel = createManualPanel();
        creationMethodTabs.addTab(TAB_MANUAL, manualPanel);

        questionsCountCombo = new JComboBox<>(Q_COUNT_OPTIONS);
        optionsCountCombo = new JComboBox<>(OPT_COUNT_OPTIONS);

        generateAiBtn = new JButton(BTN_AI_TEXT);

        aiLoadingBar = new JProgressBar(PROGRESS_BAR_MIN, PROGRESS_BAR_MAX);
        aiResultArea = new JTextArea();

        JPanel aiPanel = createAiPanel();
        creationMethodTabs.addTab(TAB_AI, aiPanel);

        add(creationMethodTabs, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        bottomPanel.add(new JLabel(LABEL_DELAY));
        delayField = new JTextField("0", DELAY_FIELD_COLUMNS);
        delayField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        bottomPanel.add(delayField);

        sendSurveyBtn = new JButton(BTN_SEND_TEXT);
        sendSurveyBtn.setFont(FONT_BOLD_14);
        applyHoverEffect(sendSurveyBtn, COLOR_BTN_DEFAULT, COLOR_BTN_HOVER_SEND);
        bottomPanel.add(sendSurveyBtn);

        countdownLabel = new JLabel("");
        countdownLabel.setFont(FONT_BOLD_14);
        countdownLabel.setForeground(Color.BLUE);
        bottomPanel.add(countdownLabel);

        add(bottomPanel, BorderLayout.SOUTH);

        generateAiBtn.addActionListener(e -> generateSurveyViaAi());
        sendSurveyBtn.addActionListener(e -> validateAndSendSurvey());
    }

    private void applyHoverEffect(JButton button, Color defaultColor, Color hoverColor) {
        button.setBackground(defaultColor);
        button.setFocusPainted(false);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(hoverColor);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(defaultColor);
            }
        });
    }

    public void setBotAndCommunity(MySurveyBot bot, List<CommunityMember> community) {
        this.myBot = bot;
        this.globalCommunity = community;
    }

    private JPanel createAiPanel() {
        JPanel panel = new JPanel(new BorderLayout(LAYOUT_GAP_SMALL, LAYOUT_GAP_SMALL));
        panel.setBorder(BorderFactory.createEmptyBorder(BORDER_PADDING_PANEL, BORDER_PADDING_PANEL, BORDER_PADDING_PANEL, BORDER_PADDING_PANEL));

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, LAYOUT_GAP_LARGE, LAYOUT_GAP_SMALL));
        configPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        configPanel.add(new JLabel(AI_LABEL_Q_COUNT));
        questionsCountCombo.setSelectedItem(Q_COUNT_OPTIONS[2]);
        configPanel.add(questionsCountCombo);

        configPanel.add(new JLabel(AI_LABEL_OPT_COUNT));
        optionsCountCombo.setSelectedItem(OPT_COUNT_OPTIONS[2]);
        configPanel.add(optionsCountCombo);

        JPanel topRow = new JPanel(new BorderLayout(LAYOUT_GAP_SMALL, LAYOUT_GAP_SMALL));
        topRow.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        topRow.add(configPanel, BorderLayout.NORTH);

        applyHoverEffect(generateAiBtn, COLOR_BTN_DEFAULT, COLOR_BTN_HOVER_AI);
        topRow.add(generateAiBtn, BorderLayout.CENTER);

        aiLoadingBar.setStringPainted(true);
        aiLoadingBar.setFont(FONT_BOLD_14);
        aiLoadingBar.setForeground(COLOR_SUCCESS);
        aiLoadingBar.setBackground(Color.WHITE);
        aiLoadingBar.setVisible(false);

        topRow.add(aiLoadingBar, BorderLayout.SOUTH);
        panel.add(topRow, BorderLayout.NORTH);

        aiResultArea.setEditable(false);
        aiResultArea.setFocusable(false);
        aiResultArea.getCaret().setVisible(false);
        aiResultArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        aiResultArea.setFont(FONT_PLAIN_16);

        JScrollPane scrollPane = new JScrollPane(aiResultArea);
        TitledBorder titledBorder = BorderFactory.createTitledBorder(AI_RESULTS_TITLE);
        titledBorder.setTitleJustification(TitledBorder.RIGHT);
        scrollPane.setBorder(titledBorder);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createManualPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(BORDER_PADDING_PANEL, BORDER_PADDING_PANEL, BORDER_PADDING_PANEL, BORDER_PADDING_PANEL));

        for (int i = 0; i < MANUAL_PANELS_COUNT; i++) {
            boolean isMandatory = (i == 0);
            manualQuestionPanels[i] = new ManualQuestionPanel(i + 1, isMandatory);
            panel.add(manualQuestionPanels[i]);
            panel.add(Box.createRigidArea(new Dimension(0, LAYOUT_GAP_MEDIUM)));
        }

        JScrollPane scroll = new JScrollPane(panel);
        scroll.setBorder(null);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void generateSurveyViaAi() {
        String topic = topicField.getText().trim();
        if (topic.isEmpty()) {
            CustomDialogs.showMessage(this, ERROR_TITLE, ERR_NO_TOPIC_AI, true);
            return;
        }

        int selectedQs = (Integer) questionsCountCombo.getSelectedItem();
        int selectedOpts = (Integer) optionsCountCombo.getSelectedItem();

        String enhancedTopic = String.format(PROMPT_TEMPLATE, topic, selectedQs, selectedOpts, selectedOpts, selectedOpts);

        aiResultArea.setText("");
        generateAiBtn.setEnabled(false);
        sendSurveyBtn.setEnabled(false);
        isAiGenerating = true;

        aiLoadingBar.setValue(PROGRESS_BAR_MIN);
        aiLoadingBar.setString(MSG_AI_CONNECTING);
        aiLoadingBar.setForeground(COLOR_PRIMARY);
        aiLoadingBar.setVisible(true);

        aiProgressTimer = new Timer(TIMER_DELAY_AI_PROGRESS, e -> {
            int current = aiLoadingBar.getValue();
            if (current < PROGRESS_MOCK_MAX_LIMIT) {
                aiLoadingBar.setValue(current + PROGRESS_MOCK_INCREMENT);
                if (current == PROGRESS_MOCK_STAGE_1) aiLoadingBar.setString(MSG_AI_WRITING);
                if (current == PROGRESS_MOCK_STAGE_2) aiLoadingBar.setString(MSG_AI_FORMATTING);
            }
        });
        aiProgressTimer.start();

        new Thread(() -> {
            try {
                generatedQuestions = aiService.generateSurveyQuestions(enhancedTopic);

                StringBuilder sb = new StringBuilder();
                sb.append(MSG_AI_SUCCESS_BODY);
                for (int i = 0; i < generatedQuestions.size(); i++) {
                    Question q = generatedQuestions.get(i);
                    sb.append("שאלה ").append(i + 1).append(": ").append(q.getText()).append("\n");
                    for (int j = 0; j < q.getOptions().size(); j++) {
                        sb.append("   ").append(j + 1).append(". ").append(q.getOptions().get(j)).append("\n");
                    }
                    sb.append("\n");
                }

                SwingUtilities.invokeLater(() -> {
                    aiProgressTimer.stop();
                    aiLoadingBar.setValue(PROGRESS_BAR_MAX);
                    aiLoadingBar.setForeground(COLOR_SUCCESS);
                    aiLoadingBar.setString(MSG_AI_SUCCESS);

                    aiResultArea.setText(sb.toString());
                    finalizeAiGeneration();

                    new Timer(TIMER_DELAY_AI_SUCCESS, evt -> {
                        aiLoadingBar.setVisible(false);
                        ((Timer)evt.getSource()).stop();
                    }).start();
                });
            } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> {
                    aiProgressTimer.stop();
                    aiLoadingBar.setValue(PROGRESS_BAR_MAX);
                    aiLoadingBar.setForeground(COLOR_ERROR);
                    aiLoadingBar.setString(MSG_AI_FAILED);

                    aiResultArea.setText(ERROR_TITLE + ":\n" + ex.getMessage());
                    finalizeAiGeneration();

                    new Timer(TIMER_DELAY_AI_ERROR, evt -> {
                        aiLoadingBar.setVisible(false);
                        ((Timer)evt.getSource()).stop();
                    }).start();
                });
            }
        }).start();
    }

    private void finalizeAiGeneration() {
        generateAiBtn.setEnabled(true);
        sendSurveyBtn.setEnabled(true);
        isAiGenerating = false;
    }

    private void validateAndSendSurvey() {
        if (isAiGenerating) {
            CustomDialogs.showMessage(this, ERROR_SYSTEM_BUSY_TITLE, ERR_AI_BUSY, true);
            return;
        }

        if (myBot != null && myBot.isSurveyActive()) {
            CustomDialogs.showMessage(this, ERROR_SYSTEM_BUSY_TITLE, ERR_ACTIVE_SURVEY, true);
            return;
        }

        if (isCountdownActive) {
            CustomDialogs.showMessage(this, ERROR_SYSTEM_BUSY_TITLE, ERR_COUNTDOWN_ACTIVE, true);
            return;
        }

        if (globalCommunity == null || globalCommunity.size() < MIN_COMMUNITY_SIZE) {
            CustomDialogs.showMessage(this, ERROR_TITLE, String.format(ERR_MIN_COMMUNITY, MIN_COMMUNITY_SIZE), true);
            return;
        }

        String topic = topicField.getText().trim();
        if (topic.isEmpty()) {
            CustomDialogs.showMessage(this, ERROR_TITLE, ERR_NO_TOPIC, true);
            return;
        }

        List<Question> finalQuestionsToSend;
        boolean isAiTabSelected = (creationMethodTabs.getSelectedIndex() == TAB_INDEX_AI);

        if (isAiTabSelected) {
            if (generatedQuestions == null || generatedQuestions.isEmpty()) {
                CustomDialogs.showMessage(this, ERROR_TITLE, ERR_AI_NOT_CREATED, true);
                return;
            }
            finalQuestionsToSend = generatedQuestions;
        } else {
            finalQuestionsToSend = new ArrayList<>();
            try {
                for (ManualQuestionPanel mqp : manualQuestionPanels) {
                    Question q = mqp.getQuestionIfValid();
                    if (q != null) finalQuestionsToSend.add(q);
                }
                if (finalQuestionsToSend.isEmpty()) {
                    throw new Exception(ERR_NO_VALID_Q);
                }
            } catch (Exception ex) {
                CustomDialogs.showMessage(this, ERR_MANUAL_FILL, ex.getMessage(), true);
                return;
            }
        }

        int delayMinutes;
        try {
            delayMinutes = Integer.parseInt(delayField.getText().trim());
            if (delayMinutes < 0 || delayMinutes > MAX_DELAY_MINUTES) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            CustomDialogs.showMessage(this, ERROR_TITLE, String.format(ERR_INVALID_DELAY, MAX_DELAY_MINUTES), true);
            return;
        }

        String msg = String.format(MSG_CONFIRM_SEND, topic, globalCommunity.size());

        boolean confirmed = CustomDialogs.showConfirm(this, CONFIRM_TITLE, msg);
        if (!confirmed) {
            return;
        }

        if (delayMinutes == 0) {
            executeSurveySend(topic, finalQuestionsToSend);
        } else {
            startCountdown(delayMinutes, topic, finalQuestionsToSend);
        }
    }

    private void executeSurveySend(String topic, List<Question> questions) {
        try {
            Survey newSurvey = new Survey(topic, questions, globalCommunity);

            myBot.startSurvey(newSurvey);
            countdownLabel.setText(MSG_COUNTDOWN_DONE);

            CustomDialogs.showMessage(this, SUCCESS_TITLE, String.format(MSG_SUCCESS_SEND, newSurvey.getTotalParticipants()), false);

            topicField.setEnabled(true);
            delayField.setEnabled(true);

        } catch (Exception ex) {
            CustomDialogs.showMessage(this, ERROR_TITLE, ex.getMessage(), true);

            sendSurveyBtn.setEnabled(true);
            generateAiBtn.setEnabled(true);
            topicField.setEnabled(true);
            delayField.setEnabled(true);
            countdownLabel.setText("");
        }
    }

    private void startCountdown(int minutes, String topic, List<Question> questions) {
        isCountdownActive = true;
        sendSurveyBtn.setEnabled(false);
        generateAiBtn.setEnabled(false);
        topicField.setEnabled(false);
        delayField.setEnabled(false);

        int totalSeconds = minutes * SECONDS_IN_MINUTE;
        long startTime = System.currentTimeMillis();

        Timer timer = new Timer(TIMER_DELAY_COUNTDOWN, null);
        timer.addActionListener(e -> {
            long elapsed = (System.currentTimeMillis() - startTime) / MILLIS_IN_SECOND;
            long remaining = totalSeconds - elapsed;
            if (remaining <= 0) {
                ((Timer)e.getSource()).stop();
                isCountdownActive = false;
                executeSurveySend(topic, questions);
                sendSurveyBtn.setEnabled(true);
                generateAiBtn.setEnabled(true);
            } else {
                countdownLabel.setText(String.format(MSG_COUNTDOWN_RUNNING, remaining / SECONDS_IN_MINUTE, remaining % SECONDS_IN_MINUTE));
            }
        });
        timer.start();
    }

    private class ManualQuestionPanel extends JPanel {
        private final JCheckBox enableCheckBox;
        private final JTextField questionField;
        private final JTextField[] optionFields;
        private final int qNum;

        public ManualQuestionPanel(int qNumber, boolean isMandatory) {
            this.qNum = qNumber;
            setLayout(new BorderLayout(LAYOUT_GAP_SMALL, LAYOUT_GAP_SMALL));
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            TitledBorder border = BorderFactory.createTitledBorder(String.format(LBL_MANUAL_Q_TITLE, qNumber));
            border.setTitleJustification(TitledBorder.RIGHT);
            setBorder(border);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            topRow.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            enableCheckBox = new JCheckBox(LBL_MANUAL_INCLUDE);
            enableCheckBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            if (isMandatory) {
                enableCheckBox.setSelected(true);
                enableCheckBox.setEnabled(false);
            } else {
                enableCheckBox.setSelected(false);
            }
            topRow.add(enableCheckBox);

            topRow.add(new JLabel(LBL_MANUAL_Q_TEXT));
            questionField = new JTextField(TOPIC_FIELD_COLUMNS);
            questionField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            topRow.add(questionField);
            add(topRow, BorderLayout.NORTH);

            JPanel optionsPanel = new JPanel(new GridLayout(GRID_OPTIONS_ROWS, GRID_OPTIONS_COLS, LAYOUT_GAP_SMALL / 2, LAYOUT_GAP_SMALL / 2));
            optionsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            optionFields = new JTextField[GRID_OPTIONS_ROWS];
            for (int i = 0; i < GRID_OPTIONS_ROWS; i++) {
                JPanel optRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                optRow.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                String labelStr = String.format(i < MIN_MANDATORY_OPTIONS ? LBL_MANUAL_OPT_MANDATORY : LBL_MANUAL_OPT_OPTIONAL, (i + 1));
                optRow.add(new JLabel(labelStr));
                optionFields[i] = new JTextField(MANUAL_OPTION_COLUMNS);
                optionFields[i].setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                optRow.add(optionFields[i]);
                optionsPanel.add(optRow);
            }
            add(optionsPanel, BorderLayout.CENTER);

            toggleFields(isMandatory);
            enableCheckBox.addActionListener(e -> toggleFields(enableCheckBox.isSelected()));
        }

        private void toggleFields(boolean enable) {
            questionField.setEnabled(enable);
            for (JTextField tf : optionFields) tf.setEnabled(enable);
        }

        public Question getQuestionIfValid() throws Exception {
            if (!enableCheckBox.isSelected()) return null;

            String qText = questionField.getText().trim();
            if (qText.isEmpty()) throw new Exception(String.format(ERR_Q_NO_TEXT, qNum));

            List<String> options = new ArrayList<>();
            for (JTextField tf : optionFields) {
                String optText = tf.getText().trim();
                if (!optText.isEmpty()) {
                    if (options.contains(optText)) {
                        throw new Exception(String.format(ERR_Q_DUPLICATE, qNum, optText));
                    }
                    options.add(optText);
                }
            }

            if (options.size() < MIN_MANDATORY_OPTIONS) throw new Exception(String.format(ERR_Q_MIN_OPTS, qNum));
            if (options.size() > GRID_OPTIONS_ROWS) throw new Exception(String.format(ERR_Q_MAX_OPTS, qNum));

            return new Question(qText, options);
        }
    }
}