package gui;

import models.CommunityMember;
import models.Survey;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.Comparator;

public class CommunityDashboard extends JFrame {

    private static final String FONT_FAMILY = "Arial";

    private static final int WINDOW_WIDTH = 750;
    private static final int WINDOW_HEIGHT = 550;
    private static final int FONT_SIZE_REGULAR = 14;
    private static final int FONT_SIZE_TITLE = 18;
    private static final int TABLE_ROW_HEIGHT = 25;

    private static final int PADDING_VERTICAL = 15;
    private static final int PADDING_HORIZONTAL = 10;

    private static final int COL_INDEX_NAME = 0;

    private static final String TITLE_DASHBOARD = "מערכת ניהול סקרים";
    private static final String TAB_COMMUNITY = "קהילת המשתמשים";
    private static final String TAB_CREATE_SURVEY = "יצירת סקר חדש";
    private static final String TAB_ACTIVE_SURVEY = "מעקב סקר פעיל";
    private static final String LABEL_TOTAL_MEMBERS = "סה\"כ חברים בקהילה: ";
    private static final String NO_USERNAME_PLACEHOLDER = "-";

    private static final String COL_NAME = "שם";
    private static final String COL_USERNAME = "Telegram Username";
    private static final String COL_JOIN_TIME = "מועד הצטרפות";

    private DefaultTableModel tableModel;
    private JLabel memberCountLabel;
    private int totalMembers = 0;

    private final SurveyCreatorPanel surveyCreatorPanel;
    private final ActiveSurveyPanel activeSurveyPanel;

    public CommunityDashboard() {
        setTitle(TITLE_DASHBOARD);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        tabbedPane.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_REGULAR));

        JPanel communityPanel = createCommunityPanel();
        tabbedPane.addTab(TAB_COMMUNITY, communityPanel);

        surveyCreatorPanel = new SurveyCreatorPanel();
        tabbedPane.addTab(TAB_CREATE_SURVEY, surveyCreatorPanel);

        activeSurveyPanel = new ActiveSurveyPanel();
        tabbedPane.addTab(TAB_ACTIVE_SURVEY, activeSurveyPanel);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createCommunityPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        memberCountLabel = new JLabel(LABEL_TOTAL_MEMBERS + totalMembers, SwingConstants.CENTER);
        memberCountLabel.setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_TITLE));
        memberCountLabel.setBorder(BorderFactory.createEmptyBorder(PADDING_VERTICAL, PADDING_HORIZONTAL, PADDING_VERTICAL, PADDING_HORIZONTAL));
        panel.add(memberCountLabel, BorderLayout.NORTH);

        String[] columnNames = {COL_NAME, COL_USERNAME, COL_JOIN_TIME};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable communityTable = new JTable(tableModel);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);

        Comparator<String> hebrewFirstComparator = (s1, s2) -> {
            boolean isHebrew1 = s1.matches(".*[א-ת].*");
            boolean isHebrew2 = s2.matches(".*[א-ת].*");

            if (isHebrew1 && !isHebrew2) {
                return -1;
            } else if (!isHebrew1 && isHebrew2) {
                return 1;
            } else {
                return s1.compareToIgnoreCase(s2);
            }
        };

        sorter.setComparator(COL_INDEX_NAME, hebrewFirstComparator);
        communityTable.setRowSorter(sorter);

        communityTable.setRowHeight(TABLE_ROW_HEIGHT);
        communityTable.setFont(new Font(FONT_FAMILY, Font.PLAIN, FONT_SIZE_REGULAR));

        communityTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        communityTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        communityTable.getTableHeader().setFont(new Font(FONT_FAMILY, Font.BOLD, FONT_SIZE_REGULAR));

        ((DefaultTableCellRenderer)communityTable.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(JLabel.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < communityTable.getColumnCount(); i++) {
            communityTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(communityTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    public void addMemberToUI(CommunityMember newMember) {
        if (newMember == null) return;

        SwingUtilities.invokeLater(() -> {
            String displayUsername;
            if (newMember.getUsername() == null || newMember.getUsername().isEmpty()) {
                displayUsername = NO_USERNAME_PLACEHOLDER;
            } else {
                displayUsername = "@" + newMember.getUsername();
            }

            Object[] rowData = { newMember.getName(), displayUsername, newMember.getJoinTime() };
            tableModel.addRow(rowData);
            totalMembers++;
            memberCountLabel.setText(LABEL_TOTAL_MEMBERS + totalMembers);
        });
    }

    public SurveyCreatorPanel getSurveyCreatorPanel() {
        return surveyCreatorPanel;
    }

    public ActiveSurveyPanel getActiveSurveyPanel() {
        return activeSurveyPanel;
    }

    public void showSurveyResults(Survey survey) {
        if (survey == null) return;

        SwingUtilities.invokeLater(() -> {
            SurveyResultsDialog dialog = new SurveyResultsDialog(this, survey);
            dialog.setVisible(true);
        });
    }
}