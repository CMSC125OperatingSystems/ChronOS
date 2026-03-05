package cmsc125.lab3.controllers;

import cmsc125.lab3.views.DashboardView;
import cmsc125.lab3.views.MainFrame;
import javax.swing.Timer;

public class AppController {
    private final MainFrame mainFrame;

    public AppController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setupButtonListeners();
    }

    private void setupButtonListeners() {
        // Get dashboard view from main frame
        DashboardView dashboard = mainFrame.getDashboardView();

        // Attach listener to Play button
        dashboard.getPlayButton().addActionListener(e -> {
            System.out.println("Play button clicked! Transition to game screen here.");
        });

        // Attach listener to How To Play button
        dashboard.getHowToPlayButton().addActionListener(e -> {
            System.out.println("How to Play button clicked!");
        });

        // Attach listener to Settings button
        dashboard.getSettingsButton().addActionListener(e -> {
            System.out.println("Settings button clicked!");
        });
    }

    // Splash screen 3 seconds delay for loading animation
    public void startApplication() {
        Timer transitionTimer = new Timer(1000, e -> mainFrame.showDashboard());

        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }
}