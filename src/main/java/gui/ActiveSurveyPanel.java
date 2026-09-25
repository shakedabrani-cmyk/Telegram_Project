package gui;

import models.Survey;
import models.SurveyParticipant;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ActiveSurveyPanel extends JPanel {

    private static final String FONT_FAMILY = "Arial";

    private static final int MAX_SURVEY_TIME_SECONDS = 300;

    private static final String STATUS_COMPLETED = "השלים";
    private static final String STATUS_IN_PROGRESS = "בתהליך";
    private static final String STATUS_NOT_STARTED = "טרם ענה";

    private static final Color COLOR_COMPLETED = new Color(210, 255, 210);
    private static final Color COLOR_IN_PROGRESS = new Color(255, 250, 205);
    private static final Color COLOR_NOT_STARTED = new Color(255, 210, 210);
    private static final Color COLOR_DEFAULT_PROGRESS_BAR = new Color(70, 130, 180);

    private final DefaultTableModel tableModel;
    private final JLabel surveyInfoLabel;
    private final JLabel participantsStatsLabel;
    private final JProgressBar timeProgressBar;

    public ActiveSurveyPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        topPanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        surveyInfoLabel = new JLabel("אין סקר פעיל כרגע.", SwingConstants.RIGHT);
        surveyInfoLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, 16));

        participantsStatsLabel = new JLabel("ממתין לתחילת סקר...", SwingConstants.RIGHT);
        participantsStatsLabel.setFont(new Font(FONT_FAMILY, Font.PLAIN, 14));

        timeProgressBar = new JProgressBar(0, MAX_SURVEY_TIME_SECONDS);
        timeProgressBar.setValue(MAX_SURVEY_TIME_SECONDS);
        timeProgressBar.setStringPainted(true);
        timeProgressBar.setString("זמן שנותר: --:--");
        timeProgressBar.setFont(new Font(FONT_FAMILY, Font.BOLD, 14));
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
        trackingTable.setRowHeight(30);
        trackingTable.setFont(new Font(FONT_FAMILY, Font.PLAIN, 14));
        trackingTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        trackingTable.getTableHeader().setFont(new Font(FONT_FAMILY, Font.BOLD, 14));

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
                    String status = (String) table.getModel().getValueAt(row, 2);
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

            surveyInfoLabel.setText("סקר פעיל בנושא: " + activeSurvey.getTopic());
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
            participantsStatsLabel.setText("משתתפים: " + totalParticipants + "  |  השלימו: " + completedCount + "  |  טרם השלימו: " + (totalParticipants - completedCount));
        });
    }

    public void updateTimeRemaining(int secondsLeft) {
        SwingUtilities.invokeLater(() -> {
            timeProgressBar.setValue(secondsLeft);
            if (secondsLeft <= 0) {
                timeProgressBar.setString("הסקר הסתיים!");
                timeProgressBar.setForeground(Color.RED);
            } else {
                long mins = secondsLeft / 60;
                long secs = secondsLeft % 60;
                timeProgressBar.setString(String.format("זמן שנותר: %02d:%02d", mins, secs));
                timeProgressBar.setForeground(COLOR_DEFAULT_PROGRESS_BAR);
            }
        });
    }
}