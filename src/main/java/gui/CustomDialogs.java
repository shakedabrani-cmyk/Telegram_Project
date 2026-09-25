package gui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class CustomDialogs {

    private static final String FONT_FAMILY = "Arial";

    private static final Color COLOR_ERROR = new Color(220, 53, 69);
    private static final Color COLOR_SUCCESS = new Color(40, 167, 69);
    private static final Color COLOR_CONFIRM = new Color(70, 130, 180);
    private static final Color COLOR_CANCEL_BG = new Color(220, 220, 220);

    private static final Font FONT_TITLE = new Font(FONT_FAMILY, Font.BOLD, 22);
    private static final Font FONT_MESSAGE = new Font(FONT_FAMILY, Font.PLAIN, 16);
    private static final Font FONT_BUTTON = new Font(FONT_FAMILY, Font.BOLD, 15);

    private static final int BORDER_THICKNESS = 3;
    private static final int PADDING_VERTICAL = 20;
    private static final int PADDING_HORIZONTAL = 30;
    private static final int BTN_WIDTH = 100;
    private static final int BTN_HEIGHT = 35;

    private CustomDialogs() {
    }

    public static void showMessage(Component parent, String title, String message, boolean isError) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);

        Color themeColor = isError ? COLOR_ERROR : COLOR_SUCCESS;
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(themeColor, BORDER_THICKNESS),
                new EmptyBorder(PADDING_VERTICAL, PADDING_HORIZONTAL, PADDING_VERTICAL, PADDING_HORIZONTAL)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(themeColor);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JTextArea messageArea = createMessageArea(message);
        mainPanel.add(messageArea, BorderLayout.CENTER);

        JButton okBtn = createStyledButton("אישור", themeColor, Color.WHITE);
        okBtn.addActionListener(e -> dialog.dispose());

        JPanel btnPanel = new JPanel();
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(okBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    public static boolean showConfirm(Component parent, String title, String message) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);

        final boolean[] result = {false};

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CONFIRM, BORDER_THICKNESS),
                new EmptyBorder(PADDING_VERTICAL, PADDING_HORIZONTAL, PADDING_VERTICAL, PADDING_HORIZONTAL)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_CONFIRM);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JTextArea messageArea = createMessageArea(message);
        mainPanel.add(messageArea, BorderLayout.CENTER);

        JButton yesBtn = createStyledButton("כן", COLOR_CONFIRM, Color.WHITE);
        yesBtn.addActionListener(e -> {
            result[0] = true;
            dialog.dispose();
        });

        JButton noBtn = createStyledButton("לא", COLOR_CANCEL_BG, Color.BLACK);
        noBtn.addActionListener(e -> {
            result[0] = false;
            dialog.dispose();
        });

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        btnPanel.setBackground(Color.WHITE);
        btnPanel.add(yesBtn);
        btnPanel.add(noBtn);
        mainPanel.add(btnPanel, BorderLayout.SOUTH);

        dialog.add(mainPanel);
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    private static JTextArea createMessageArea(String message) {
        JTextArea messageArea = new JTextArea(message);
        messageArea.setFont(FONT_MESSAGE);
        messageArea.setEditable(false);
        messageArea.setFocusable(false);
        messageArea.setHighlighter(null);

        messageArea.getCaret().setVisible(false);
        messageArea.getCaret().setBlinkRate(0);
        messageArea.setCaretColor(Color.WHITE);
        messageArea.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

        messageArea.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        messageArea.setBackground(Color.WHITE);

        return messageArea;
    }

    private static JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(BTN_WIDTH, BTN_HEIGHT));
        btn.setBorder(BorderFactory.createLineBorder(bg.darker(), 1));

        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(bg.brighter());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }
}