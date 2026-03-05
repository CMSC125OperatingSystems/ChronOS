package cmsc125.lab3.views;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

public class MainFrame extends JFrame {
    // Constants for CardLayout routing
    private static final String VIEW_SPLASH = "SPLASH";
    private static final String VIEW_DASHBOARD = "DASHBOARD";

    private final CardLayout cardLayout;
    private final JPanel cardPanel;
    private DashboardView dashboardView;

    public MainFrame() {
        setTitle("ChronOS");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH); // Open in full screen

        // Set up CardLayout for panel swaps
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // Instantiate Panel Views
        SplashView splashView = new SplashView();
        dashboardView = new DashboardView();

        cardPanel.add(splashView, VIEW_SPLASH);
        cardPanel.add(dashboardView, VIEW_DASHBOARD);
        add(cardPanel);
    }

    // Method called by Controller to switch to Dashboard
    public void showDashboard() {
        cardLayout.show(cardPanel, VIEW_DASHBOARD);
    }

    public DashboardView getDashboardView() {
        return dashboardView;
    }
}