package cmsc125.lab3.views;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.CardLayout;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cardPanel;

    private final SplashView splashView;
    private final DashboardView dashboardView;
    private final SettingsView settingsView;
    private final SimulatorSetupView setupView;
    private final SimulationView simulationView;

    public MainFrame() {
        setTitle("ChronOS");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        splashView = new SplashView();
        dashboardView = new DashboardView();
        settingsView = new SettingsView();
        setupView = new SimulatorSetupView();
        simulationView = new SimulationView();

        cardPanel.add(splashView, "SPLASH");
        cardPanel.add(dashboardView, "DASHBOARD");
        cardPanel.add(settingsView, "SETTINGS");
        cardPanel.add(setupView, "SETUP");
        cardPanel.add(simulationView, "SIMULATION");

        add(cardPanel);
    }

    public void showDashboard() {
        cardLayout.show(cardPanel, "DASHBOARD");
        splashView.stopAnimation();
    }

    public void showSettings() { cardLayout.show(cardPanel, "SETTINGS"); }
    public void showSetup() { cardLayout.show(cardPanel, "SETUP"); }
    public void showSimulation() { cardLayout.show(cardPanel, "SIMULATION"); }

    public DashboardView getDashboardView() { return dashboardView; }
    public SettingsView getSettingsView() { return settingsView; }
    public SimulatorSetupView getSetupView() { return setupView; }
    public SimulationView getSimulationView() { return simulationView; }
}