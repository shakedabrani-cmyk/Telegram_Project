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

    private static final int MIN_COMMUNITY_SIZE = 1;
    private static final int MAX_DELAY_MINUTES = 1440;

    private static final int TIMER_DELAY_AI_PROGRESS = 150;
    private static final int TIMER_DELAY_AI_SUCCESS = 1500;
    private static final int TIMER_DELAY_AI_ERROR = 2000;
    private static final int TIMER_DELAY_COUNTDOWN = 1000;

    private static final Font FONT_BOLD_14 = new Font(FONT_FAMILY, Font.BOLD, 14);
    private static final Font FONT_PLAIN_16 = new Font(FONT_FAMILY, Font.PLAIN, 16);

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

    private static final String ERROR_SYSTEM_BUSY_TITLE = "מערכת עסוקה";
    private static final String ERROR_TITLE = "שגיאה";
    private static final String SUCCESS_TITLE = "הצלחה";
    private static final String CONFIRM_TITLE = "אישור שילוח סקר";

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
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JPanel topicPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topicPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        topicPanel.add(new JLabel(LABEL_TOPIC));

        topicField = new JTextField(25);
        topicField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        topicField.setFont(FONT_BOLD_14);
        topicPanel.add(topicField);
        add(topicPanel, BorderLayout.NORTH);

        creationMethodTabs = new JTabbedPane();
        creationMethodTabs.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        creationMethodTabs.setFont(FONT_BOLD_14);

        manualQuestionPanels = new ManualQuestionPanel[3];
        JPanel manualPanel = createManualPanel();
        creationMethodTabs.addTab(TAB_MANUAL, manualPanel);

        questionsCountCombo = new JComboBox<>(new Integer[]{1, 2, 3});
        optionsCountCombo = new JComboBox<>(new Integer[]{2, 3, 4});
        generateAiBtn = new JButton(BTN_AI_TEXT);
        aiLoadingBar = new JProgressBar(0, 100);
        aiResultArea = new JTextArea();

        JPanel aiPanel = createAiPanel();
        creationMethodTabs.addTab(TAB_AI, aiPanel);

        add(creationMethodTabs, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        bottomPanel.add(new JLabel(LABEL_DELAY));
        delayField = new JTextField("0", 3);
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
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel configPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 5));
        configPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        configPanel.add(new JLabel(AI_LABEL_Q_COUNT));
        questionsCountCombo.setSelectedItem(3);
        configPanel.add(questionsCountCombo);

        configPanel.add(new JLabel(AI_LABEL_OPT_COUNT));
        optionsCountCombo.setSelectedItem(4);
        configPanel.add(optionsCountCombo);

        JPanel topRow = new JPanel(new BorderLayout(5, 5));
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
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (int i = 0; i < 3; i++) {
            boolean isMandatory = (i == 0);
            manualQuestionPanels[i] = new ManualQuestionPanel(i + 1, isMandatory);
            panel.add(manualQuestionPanels[i]);
            panel.add(Box.createRigidArea(new Dimension(0, 10)));
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
            CustomDialogs.showMessage(this, ERROR_TITLE, "אנא הזן נושא לסקר בתיבה העליונה.", true);
            return;
        }

        int selectedQs = (Integer) questionsCountCombo.getSelectedItem();
        int selectedOpts = (Integer) optionsCountCombo.getSelectedItem();

        String enhancedTopic = String.format(PROMPT_TEMPLATE, topic, selectedQs, selectedOpts, selectedOpts, selectedOpts);

        aiResultArea.setText("");
        generateAiBtn.setEnabled(false);
        sendSurveyBtn.setEnabled(false);
        isAiGenerating = true;

        aiLoadingBar.setValue(0);
        aiLoadingBar.setString(MSG_AI_CONNECTING);
        aiLoadingBar.setForeground(COLOR_PRIMARY);
        aiLoadingBar.setVisible(true);

        aiProgressTimer = new Timer(TIMER_DELAY_AI_PROGRESS, e -> {
            int current = aiLoadingBar.getValue();
            if (current < 90) {
                aiLoadingBar.setValue(current + 1);
                if (current == 30) aiLoadingBar.setString(MSG_AI_WRITING);
                if (current == 60) aiLoadingBar.setString(MSG_AI_FORMATTING);
            }
        });
        aiProgressTimer.start();

        new Thread(() -> {
            try {
                generatedQuestions = aiService.generateSurveyQuestions(enhancedTopic);

                StringBuilder sb = new StringBuilder();
                sb.append("הסקר נוצר בהצלחה!\n\n");
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
                    aiLoadingBar.setValue(100);
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
                    aiLoadingBar.setValue(100);
                    aiLoadingBar.setForeground(COLOR_ERROR);
                    aiLoadingBar.setString(MSG_AI_FAILED);

                    aiResultArea.setText("שגיאה:\n" + ex.getMessage());
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
            CustomDialogs.showMessage(this, ERROR_SYSTEM_BUSY_TITLE, "המערכת מייצרת כעת שאלות AI. אנא המתן לסיום התהליך.", true);
            return;
        }

        if (myBot != null && myBot.isSurveyActive()) {
            CustomDialogs.showMessage(this, ERROR_SYSTEM_BUSY_TITLE, "שגיאה: קיים סקר פעיל כרגע.\nלפי ההנחיות ניתן לנהל סקר אחד בלבד בכל פעם.", true);
            return;
        }

        if (isCountdownActive) {
            CustomDialogs.showMessage(this, ERROR_SYSTEM_BUSY_TITLE, "שגיאה: קיים סקר שממתין לשילוח (ספירה לאחור רצה).\nלא ניתן להוסיף סקר חדש.", true);
            return;
        }

        if (globalCommunity == null || globalCommunity.size() < MIN_COMMUNITY_SIZE) {
            CustomDialogs.showMessage(this, ERROR_TITLE, "שגיאה: דרוש לפחות " + MIN_COMMUNITY_SIZE + " חברי קהילה כדי להתחיל סקר.", true);
            return;
        }

        String topic = topicField.getText().trim();
        if (topic.isEmpty()) {
            CustomDialogs.showMessage(this, ERROR_TITLE, "יש להזין נושא כללי לסקר בראש המסך.", true);
            return;
        }

        List<Question> finalQuestionsToSend;
        boolean isAiTabSelected = (creationMethodTabs.getSelectedIndex() == 1);

        if (isAiTabSelected) {
            if (generatedQuestions == null || generatedQuestions.isEmpty()) {
                CustomDialogs.showMessage(this, ERROR_TITLE, "יש ליצור שאלות AI לפני השילוח.", true);
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
                    throw new Exception("יש למלא לפחות שאלה אחת תקינה.");
                }
            } catch (Exception ex) {
                CustomDialogs.showMessage(this, "שגיאה במילוי ידני", ex.getMessage(), true);
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
            CustomDialogs.showMessage(this, ERROR_TITLE, "הזן מספר דקות תקין להשהייה (בין 0 ל-" + MAX_DELAY_MINUTES + ").", true);
            return;
        }

        String msg = String.format("האם אתה בטוח שברצונך לשגר את הסקר בנושא:\n'%s'\nל-%d משתתפים?", topic, globalCommunity.size());

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
        Survey newSurvey = new Survey(topic, questions, globalCommunity);
        myBot.startSurvey(newSurvey);
        countdownLabel.setText("הסקר נשלח!");

        CustomDialogs.showMessage(this, SUCCESS_TITLE, "הסקר נשלח בהצלחה ל-" + newSurvey.getTotalParticipants() + " משתתפים!", false);

        topicField.setEnabled(true);
        delayField.setEnabled(true);
    }

    private void startCountdown(int minutes, String topic, List<Question> questions) {
        isCountdownActive = true;
        sendSurveyBtn.setEnabled(false);
        generateAiBtn.setEnabled(false);
        topicField.setEnabled(false);
        delayField.setEnabled(false);

        int totalSeconds = minutes * 60;
        long startTime = System.currentTimeMillis();

        Timer timer = new Timer(TIMER_DELAY_COUNTDOWN, null);
        timer.addActionListener(e -> {
            long elapsed = (System.currentTimeMillis() - startTime) / 1000;
            long remaining = totalSeconds - elapsed;
            if (remaining <= 0) {
                ((Timer)e.getSource()).stop();
                isCountdownActive = false;
                executeSurveySend(topic, questions);
                sendSurveyBtn.setEnabled(true);
                generateAiBtn.setEnabled(true);
            } else {
                countdownLabel.setText(String.format("הסקר יישלח בעוד: %02d:%02d", remaining / 60, remaining % 60));
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
            setLayout(new BorderLayout(5, 5));
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            TitledBorder border = BorderFactory.createTitledBorder("שאלה " + qNumber);
            border.setTitleJustification(TitledBorder.RIGHT);
            setBorder(border);

            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            topRow.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

            enableCheckBox = new JCheckBox("כלול שאלה זו");
            enableCheckBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            if (isMandatory) {
                enableCheckBox.setSelected(true);
                enableCheckBox.setEnabled(false);
            } else {
                enableCheckBox.setSelected(false);
            }
            topRow.add(enableCheckBox);

            topRow.add(new JLabel("נוסח השאלה:"));
            questionField = new JTextField(25);
            questionField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            topRow.add(questionField);
            add(topRow, BorderLayout.NORTH);

            JPanel optionsPanel = new JPanel(new GridLayout(4, 1, 2, 2));
            optionsPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            optionFields = new JTextField[4];
            for (int i = 0; i < 4; i++) {
                JPanel optRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                optRow.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                String labelStr = "אפשרות " + (i + 1) + (i < 2 ? " (חובה):" : " (רשות):");
                optRow.add(new JLabel(labelStr));
                optionFields[i] = new JTextField(20);
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
            if (qText.isEmpty()) throw new Exception("בשאלה " + qNum + " חסר נוסח השאלה.");

            List<String> options = new ArrayList<>();
            for (JTextField tf : optionFields) {
                String optText = tf.getText().trim();
                if (!optText.isEmpty()) {
                    if (options.contains(optText)) {
                        throw new Exception("בשאלה " + qNum + " יש כפילות: התשובה '" + optText + "' מופיעה פעמיים. נא לתקן.");
                    }
                    options.add(optText);
                }
            }

            if (options.size() < 2) throw new Exception("בשאלה " + qNum + " חובה להזין לפחות 2 אפשרויות תשובה.");
            if (options.size() > 4) throw new Exception("בשאלה " + qNum + " ניתן להזין עד 4 אפשרויות בלבד.");

            return new Question(qText, options);
        }
    }
}