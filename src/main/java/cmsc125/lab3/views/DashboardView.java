package cmsc125.lab3.views;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import javax.swing.BoxLayout;
import javax.swing.Box;
import javax.swing.ImageIcon;
import java.awt.FlowLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.net.URL;

public class DashboardView extends JPanel {
    private JButton playButton, howToPlayButton, settingsButton;

    public DashboardView() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("ChronOS");
        titleLabel.setFont(new Font("Serif", Font.BOLD, 75));
        titleLabel.setForeground(Color.black);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel taglineLabel = new JLabel("Master the Gears of Time");
        taglineLabel.setFont(new Font("Serif", Font.ITALIC, 25));
        taglineLabel.setForeground(Color.black);
        taglineLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel iconsPanel = new JPanel();
        iconsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 250, 0));
        iconsPanel.setOpaque(false);

        playButton = createIconButton("Play", "play.png");
        howToPlayButton = createIconButton("How to Play", "htp.png");
        settingsButton = createIconButton("Settings", "settings.png");

        iconsPanel.add(playButton);
        iconsPanel.add(howToPlayButton);
        iconsPanel.add(settingsButton);

        add(Box.createVerticalStrut(100));
        add(titleLabel);
        add(Box.createVerticalStrut(10));
        add(taglineLabel);
        add(Box.createVerticalStrut(500));
        add(iconsPanel);
    }

    // Method to create  icon with text beneath it
    private JButton createIconButton(String labelText, String iconFileName) {
        JButton button = new JButton(labelText);

        // Position text directly below icon
        button.setVerticalTextPosition(SwingConstants.BOTTOM);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFont(new Font("Serif", Font.BOLD, 20));
        button.setForeground(new Color(43, 29, 20));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Load and scale image
        URL imgURL = getClass().getResource("/icons/" + iconFileName);
        ImageIcon icon = new ImageIcon(imgURL);
        Image img = icon.getImage();
        Image scaledImg = img.getScaledInstance(100, 100, Image.SCALE_SMOOTH);
        button.setIcon(new ImageIcon(scaledImg));

        return button;
    }

    // Button getters
    public JButton getPlayButton() { return playButton; }
    public JButton getHowToPlayButton() { return howToPlayButton; }
    public JButton getSettingsButton() { return settingsButton; }
}