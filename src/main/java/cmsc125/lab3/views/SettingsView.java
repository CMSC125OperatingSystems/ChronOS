package cmsc125.lab3.views;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class SettingsView extends JPanel {
    public static final Color ENABLED_GREEN = new Color(175, 255, 175);
    public static final Color DISABLED_RED = new Color(255, 175, 175);

    private final JButton bgmToggleBtn, sfxToggleBtn, darkModeToggleBtn, backBtn;
    private final JSlider bgmSlider, sfxSlider;
    private final JSpinner bgmSpinner, sfxSpinner;

    public SettingsView() {
        setLayout(new BorderLayout());

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Settings");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 50));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel bgmRow = createSettingsRow("Background Music (BGM)");
        bgmToggleBtn = (JButton) bgmRow.getClientProperty("toggleBtn");
        bgmSlider = (JSlider) bgmRow.getClientProperty("slider");
        bgmSpinner = (JSpinner) bgmRow.getClientProperty("spinner");

        JPanel sfxRow = createSettingsRow("Sound Effects (SFX)");
        sfxToggleBtn = (JButton) sfxRow.getClientProperty("toggleBtn");
        sfxSlider = (JSlider) sfxRow.getClientProperty("slider");
        sfxSpinner = (JSpinner) sfxRow.getClientProperty("spinner");

        JPanel darkModeRow = createToggleRow("Dark Mode Theme");
        darkModeToggleBtn = (JButton) darkModeRow.getClientProperty("toggleBtn");

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(titleLabel);
        centerPanel.add(Box.createVerticalStrut(50));
        centerPanel.add(bgmRow);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(sfxRow);
        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(darkModeRow);
        centerPanel.add(Box.createVerticalGlue());

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 30));
        bottomPanel.setOpaque(false);

        backBtn = createIconButton("Back", "back.png");
        bottomPanel.add(backBtn);

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createSettingsRow(String labelText) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Serif", Font.PLAIN, 20));
        label.setPreferredSize(new Dimension(250, 30));

        JButton toggleBtn = new JButton("Enabled");
        toggleBtn.setBackground(ENABLED_GREEN);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setPreferredSize(new Dimension(120, 40));

        JSlider slider = new JSlider(0, 100, 100);
        slider.setPreferredSize(new Dimension(300, 40));

        SpinnerNumberModel spinnerModel = new SpinnerNumberModel(100, 0, 100, 1);
        JSpinner spinner = new JSpinner(spinnerModel);
        spinner.setPreferredSize(new Dimension(60, 30));

        row.add(label);
        row.add(toggleBtn);
        row.add(slider);
        row.add(spinner);

        row.putClientProperty("toggleBtn", toggleBtn);
        row.putClientProperty("slider", slider);
        row.putClientProperty("spinner", spinner);

        return row;
    }

    private JPanel createToggleRow(String labelText) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        row.setOpaque(false);

        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Serif", Font.PLAIN, 20));
        label.setPreferredSize(new Dimension(250, 30));

        JButton toggleBtn = new JButton("Enabled");
        toggleBtn.setBackground(ENABLED_GREEN);
        toggleBtn.setFocusPainted(false);
        toggleBtn.setPreferredSize(new Dimension(120, 40));

        row.add(label);
        row.add(toggleBtn);
        row.putClientProperty("toggleBtn", toggleBtn);

        return row;
    }

    private JButton createIconButton(String labelText, String iconFileName) {
        JButton button = new JButton(labelText);
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFont(new Font("Serif", Font.BOLD, 18));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        URL imgURL = getClass().getResource("/icons/" + iconFileName);
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            Image img = icon.getImage();
            Image scaledImg = img.getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            button.setIcon(new ImageIcon(scaledImg));
        } else System.err.println("Warning: Could not find image " + iconFileName);

        return button;
    }

    public JButton getBgmToggleBtn() { return bgmToggleBtn; }
    public JButton getSfxToggleBtn() { return sfxToggleBtn; }
    public JButton getDarkModeToggleBtn() { return darkModeToggleBtn; }
    public JButton getBackBtn() { return backBtn; }
    public JSlider getBgmSlider() { return bgmSlider; }
    public JSlider getSfxSlider() { return sfxSlider; }
    public JSpinner getBgmSpinner() { return bgmSpinner; }
    public JSpinner getSfxSpinner() { return sfxSpinner; }
}