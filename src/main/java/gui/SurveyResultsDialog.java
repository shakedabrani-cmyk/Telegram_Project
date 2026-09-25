package gui;

import models.Question;
import models.Survey;
import models.SurveyParticipant;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SurveyResultsDialog extends JDialog {

    private static final int WINDOW_WIDTH = 550;
    private static final int WINDOW_HEIGHT = 650;

    private static final int PADDING_MAIN = 20;
    private static final int PADDING_SMALL = 5;
    private static final int SPACING_LARGE = 20;
    private static final int SPACING_MEDIUM = 15;
    private static final int SPACING_SMALL = 10;
    private static final int MAX_OPTION_WIDTH = 500;
    private static final int MAX_OPTION_HEIGHT = 50;

    private static final int PROGRESS_MIN = 0;
    private static final int PROGRESS_MAX = 100;
    private static final int PERCENTAGE_MULTIPLIER = 100;
    private static final int INITIAL_VOTES = 0;

    private static final String FONT_FAMILY = "Arial";
    private static final int FONT_SIZE_HEADER = 22;
    private static final int FONT_SIZE_QUESTION = 18;
    private static final int FONT_SIZE_OPTION = 15;
    private static final int FONT_SIZE_PROGRESS = 13;

    private static final Color COLOR_PROGRESS_FG = new Color(70, 130, 180);
    private static final Color COLOR_PROGRESS_BG = Color.WHITE;

    private static final String TITLE_PREFIX = "תוצאות הסקר: ";
    private static final String QUESTION_PREFIX = "שאלה ";
    private static final String CLOSE_BUTTON_TEXT = "סגור נתונים";
    private static final String VOTES_FORMAT = "%s (%d הצבעות)";
    private static final String PERCENT_FORMAT = "%.1f%%";

    public SurveyResultsDialog(JFrame parent, Survey survey) {
        super(parent, TITLE_PREFIX + (survey != null ? survey.getTopic() : ""), true);

        if (survey == null) {
            dispose();
            return;
        }

        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(PADDING_MAIN, PADDING_MAIN, PADDING_MAIN, PADDING_MAIN));
        mainPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel headerLabel = new JLabel(TITLE_PREFIX + survey.getTopic());
        headerLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_HEADER));
        headerLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        mainPanel.add(headerLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, SPACING_LARGE)));

        List<Question> questions = survey.getQuestions();
        if (questions != null) {
            for (int i = 0; i < questions.size(); i++) {
                Question q = questions.get(i);

                if (q == null || q.getOptions() == null) {
                    continue;
                }

                Map<Integer, Integer> votesCount = new HashMap<>();
                int totalVotesForQuestion = INITIAL_VOTES;

                for (int j = 0; j < q.getOptions().size(); j++) {
                    votesCount.put(j, INITIAL_VOTES);
                }

                for (SurveyParticipant p : survey.getParticipants()) {
                    if (p != null && p.getAnswers() != null && p.getAnswers().size() > i) {
                        int chosenOption = p.getAnswers().get(i);
                        if (votesCount.containsKey(chosenOption)) {
                            votesCount.put(chosenOption, votesCount.get(chosenOption) + 1);
                            totalVotesForQuestion++;
                        }
                    }
                }

                List<OptionResult> resultsList = new ArrayList<>();
                for (int j = 0; j < q.getOptions().size(); j++) {
                    int votes = votesCount.get(j);
                    double percent = totalVotesForQuestion == INITIAL_VOTES ? INITIAL_VOTES : ((double) votes / totalVotesForQuestion) * PERCENTAGE_MULTIPLIER;
                    resultsList.add(new OptionResult(q.getOptions().get(j), votes, percent));
                }

                Collections.sort(resultsList);

                JLabel qLabel = new JLabel(QUESTION_PREFIX + (i + 1) + ": " + q.getText());
                qLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_QUESTION));
                qLabel.setAlignmentX(Component.RIGHT_ALIGNMENT);
                mainPanel.add(qLabel);
                mainPanel.add(Box.createRigidArea(new Dimension(0, SPACING_SMALL)));

                for (OptionResult res : resultsList) {
                    JPanel optionPanel = new JPanel(new BorderLayout(PADDING_SMALL, PADDING_SMALL));
                    optionPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                    optionPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
                    optionPanel.setMaximumSize(new Dimension(MAX_OPTION_WIDTH, MAX_OPTION_HEIGHT));

                    String text = String.format(VOTES_FORMAT, res.getText(), res.getVotes());
                    JLabel resLabel = new JLabel(text);
                    resLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_OPTION));
                    resLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

                    JProgressBar progressBar = new JProgressBar(PROGRESS_MIN, PROGRESS_MAX);
                    progressBar.setValue((int) res.getPercentage());
                    progressBar.setStringPainted(true);
                    progressBar.setString(String.format(PERCENT_FORMAT, res.getPercentage()));
                    progressBar.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_PROGRESS));
                    progressBar.setForeground(COLOR_PROGRESS_FG);
                    progressBar.setBackground(COLOR_PROGRESS_BG);

                    optionPanel.add(resLabel, BorderLayout.NORTH);
                    optionPanel.add(progressBar, BorderLayout.CENTER);

                    mainPanel.add(optionPanel);
                    mainPanel.add(Box.createRigidArea(new Dimension(0, SPACING_MEDIUM)));
                }
                mainPanel.add(Box.createRigidArea(new Dimension(0, SPACING_SMALL)));
            }
        }

        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);

        JButton closeBtn = new JButton(CLOSE_BUTTON_TEXT);
        closeBtn.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_OPTION));
        closeBtn.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private static class OptionResult implements Comparable<OptionResult> {
        private final String text;
        private final int votes;
        private final double percentage;

        public OptionResult(String text, int votes, double percentage) {
            this.text = text;
            this.votes = votes;
            this.percentage = percentage;
        }

        public String getText() {
            return text;
        }

        public int getVotes() {
            return votes;
        }

        public double getPercentage() {
            return percentage;
        }

        @Override
        public int compareTo(OptionResult other) {
            return Double.compare(other.percentage, this.percentage);
        }
    }
}