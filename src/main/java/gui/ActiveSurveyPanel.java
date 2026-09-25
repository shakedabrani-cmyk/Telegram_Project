package gui;

import models.Survey;
import models.SurveyParticipant;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;

public class ActiveSurveyPanel extends JPanel {

    private static final String FONT_FAMILY = "Arial";

    private static final int MAX_SURVEY_TIME_SECONDS = 300;
    private static final int SECONDS_IN_MINUTE = 60;

    private static final int PADDING_MAIN = 20;
    private static final int PADDING_PANEL = 10;
    private static final int GAP_SMALL = 5;

    private static final int FONT_SIZE_LARGE = 16;
    private static final int FONT_SIZE_REGULAR = 14;

    private static final int TABLE_ROW_HEIGHT = 30;
    private static final int GRID_ROWS = 3;
    private static final int GRID_COLS = 1;

    private static final int COL_INDEX_STATUS = 2;

    private static final String STATUS_COMPLETED = "השלים";
    private static final String STATUS_IN_PROGRESS = "בתהליך";
    private static final String STATUS_NOT_STARTED = "טרם ענה";

    private static final String LBL_NO_SURVEY = "אין סקר פעיל כרגע.";
    private static final String LBL_WAITING = "ממתין לתחילת סקר...";
    private static final String LBL_TIME_PLACEHOLDER = "זמן שנותר: --:--";
    private static final String LBL_SURVEY_TOPIC = "סקר פעיל בנושא: %s";
    private static final String LBL_STATS_FORMAT = "משתתפים: %d  |  השלימו: %d  |  טרם השלימו: %d";
    private static final String LBL_TIME_ENDED = "הסקר הסתיים!";
    private static final String LBL_TIME_REMAINING = "זמן שנותר: %02d:%02d";

    private static final Color COLOR_COMPLETED = new Color(210, 255, 210);
    private static final Color COLOR_IN_PROGRESS = new Color(255, 250, 205);
    private static final Color COLOR_NOT_STARTED = new Color(255, 210, 210);
    private static final Color COLOR_DEFAULT_PROGRESS_BAR = new Color(70, 130, 180);

    private final DefaultTableModel tableModel;
    private final JLabel surveyInfoLabel;
    private final JLabel participantsStatsLabel;
    private final JProgressBar timeProgressBar;

    public ActiveSurveyPanel() {
        setLayout(new BorderLayout(PADDING_PANEL, PADDING_PANEL));
        setBorder(BorderFactory.createEmptyBorder(PADDING_MAIN, PADDING_MAIN, PADDING_MAIN, PADDING_MAIN));

        JPanel topPanel = new JPanel(new GridLayout(GRID_ROWS, GRID_COLS, GAP_SMALL, GAP_SMALL));
        topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        surveyInfoLabel = new JLabel(LBL_NO_SURVEY, SwingConstants.RIGHT);
        surveyInfoLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_LARGE));

        participantsStatsLabel = new JLabel(LBL_WAITING, SwingConstants.RIGHT);
        participantsStatsLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_REGULAR));

        timeProgressBar = new JProgressBar(0, MAX_SURVEY_TIME_SECONDS);
        timeProgressBar.setValue(MAX_SURVEY_TIME_SECONDS);
        timeProgressBar.setStringPainted(true);
        timeProgressBar.setString(LBL_TIME_PLACEHOLDER);
        timeProgressBar.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_REGULAR));
        timeProgressBar.setForeground(COLOR_DEFAULT_PROGRESS_BAR);

        topPanel.add(surveyInfoLabel);
        topPanel.add(participantsStatsLabel);
        topPanel.add(timeProgressBar);
        add(topPanel, BorderLayout.NORTH);

        String[] columnNames = {"שם המשתתף", "התקדמות", "מצב"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable trackingTable = new JTable(tableModel);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        trackingTable.setRowSorter(sorter);

        trackingTable.setRowHeight(TABLE_ROW_HEIGHT);
        trackingTable.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_REGULAR));
        trackingTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        trackingTable.getTableHeader().setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_REGULAR));

        ((DefaultTableCellRenderer)trackingTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer smartColorRenderer = createStatusColorRenderer();

        for (int i = 0; i < trackingTable.getColumnCount(); i++) {
            trackingTable.getColumnModel().getColumn(i).setCellRenderer(smartColorRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(trackingTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private DefaultTableCellRenderer createStatusColorRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(JLabel.CENTER);

                if (!isSelected) {
                    String status = (String) table.getValueAt(row, COL_INDEX_STATUS);
                    if (STATUS_COMPLETED.equals(status)) {
                        c.setBackground(COLOR_COMPLETED);
                    } else if (STATUS_IN_PROGRESS.equals(status)) {
                        c.setBackground(COLOR_IN_PROGRESS);
                    } else if (STATUS_NOT_STARTED.equals(status)) {
                        c.setBackground(COLOR_NOT_STARTED);
                    } else {
                        c.setBackground(Color.WHITE);
                    }
                }
                return c;
            }
        };
    }

    public void updateSurveyStatus(Survey activeSurvey) {
        SwingUtilities.invokeLater(() -> {
            if (activeSurvey == null) return;

            surveyInfoLabel.setText(String.format(LBL_SURVEY_TOPIC, activeSurvey.getTopic()));
            int totalParticipants = activeSurvey.getTotalParticipants();
            int totalQuestions = activeSurvey.getQuestions().size();
            int completedCount = 0;

            tableModel.setRowCount(0);

            for (SurveyParticipant p : activeSurvey.getParticipants()) {
                int answeredCount = p.getAnswers().size();
                String status;

                if (answeredCount == 0) {
                    status = STATUS_NOT_STARTED;
                } else if (answeredCount < totalQuestions) {
                    status = STATUS_IN_PROGRESS;
                } else {
                    status = STATUS_COMPLETED;
                    completedCount++;
                }

                String progress = answeredCount + "/" + totalQuestions;
                tableModel.addRow(new Object[]{p.getMember().getName(), progress, status});
            }

            participantsStatsLabel.setText(String.format(LBL_STATS_FORMAT, totalParticipants, completedCount, (totalParticipants - completedCount)));
        });
    }

    public void updateTimeRemaining(int secondsLeft) {
        SwingUtilities.invokeLater(() -> {
            timeProgressBar.setValue(secondsLeft);
            if (secondsLeft <= 0) {
                timeProgressBar.setString(LBL_TIME_ENDED);
                timeProgressBar.setForeground(Color.RED);
            } else {
                long mins = secondsLeft / SECONDS_IN_MINUTE;
                long secs = secondsLeft % SECONDS_IN_MINUTE;
                timeProgressBar.setString(String.format(LBL_TIME_REMAINING, mins, secs));
                timeProgressBar.setForeground(COLOR_DEFAULT_PROGRESS_BAR);
            }
        });
    }
}