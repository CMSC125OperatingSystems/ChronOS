package cmsc125.lab3.controllers;

import cmsc125.lab3.models.SettingsModel;
import cmsc125.lab3.models.ProcessModel;
import cmsc125.lab3.services.*;
import cmsc125.lab3.views.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class AppController {
    private final MainFrame mainFrame;
    private final AudioService audioService;
    private final SettingsModel settingsModel;

    private Timer simulationTimer;
    private BaseSimulator currentSimulator;
    private List<ProcessModel> currentProcesses;

    public AppController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.audioService = new AudioService();
        this.settingsModel = new SettingsModel();

        syncSettingsViewWithModel();

        mainFrame.getDashboardView().getPlayButton().addActionListener(e -> mainFrame.showSetup());
        mainFrame.getDashboardView().getSettingsButton().addActionListener(e -> mainFrame.showSettings());
        mainFrame.getDashboardView().getExitButton().addActionListener(e -> confirmAndExit());

        mainFrame.getSettingsView().getBackBtn().addActionListener(e -> mainFrame.showDashboard());

        setupSettingsListeners();
        setupSimulationListeners();
        setupWindowExitListener();
    }

    private void syncSettingsViewWithModel() {
        SettingsView settings = mainFrame.getSettingsView();
        updateToggleButton(settings.getSfxToggleBtn(), settingsModel.isSfxEnabled());
        settings.getSfxSlider().setValue(settingsModel.getSfxVolume());
        settings.getSfxSpinner().setValue(settingsModel.getSfxVolume());

        updateToggleButton(settings.getBgmToggleBtn(), settingsModel.isBgmEnabled());
        settings.getBgmSlider().setValue(settingsModel.getBgmVolume());
        settings.getBgmSpinner().setValue(settingsModel.getBgmVolume());

        updateToggleButton(settings.getDarkModeToggleBtn(), settingsModel.isDarkModeEnabled());
        ThemeManager.applyTheme(mainFrame, settingsModel.isDarkModeEnabled());
    }

    private void setupSettingsListeners() {
        SettingsView settings = mainFrame.getSettingsView();

        settings.getSfxToggleBtn().addActionListener(e -> {
            boolean newState = !settingsModel.isSfxEnabled();
            settingsModel.setSfxEnabled(newState);
            updateToggleButton(settings.getSfxToggleBtn(), newState);
            audioService.updateSfxVolume(settingsModel.getSfxVolume(), newState);
        });

        settings.getSfxSlider().addChangeListener(e -> {
            int val = settings.getSfxSlider().getValue();
            settingsModel.setSfxVolume(val);
            settings.getSfxSpinner().setValue(val);
            audioService.updateSfxVolume(val, settingsModel.isSfxEnabled());
        });

        settings.getSfxSpinner().addChangeListener(e -> {
            int val = (Integer) settings.getSfxSpinner().getValue();
            settingsModel.setSfxVolume(val);
            settings.getSfxSlider().setValue(val);
            audioService.updateSfxVolume(val, settingsModel.isSfxEnabled());
        });

        settings.getBgmToggleBtn().addActionListener(e -> {
            boolean newState = !settingsModel.isBgmEnabled();
            settingsModel.setBgmEnabled(newState);
            updateToggleButton(settings.getBgmToggleBtn(), newState);
        });

        settings.getBgmSlider().addChangeListener(e -> {
            int val = settings.getBgmSlider().getValue();
            settingsModel.setBgmVolume(val);
            settings.getBgmSpinner().setValue(val);
        });

        settings.getBgmSpinner().addChangeListener(e -> {
            int val = (Integer) settings.getBgmSpinner().getValue();
            settingsModel.setBgmVolume(val);
            settings.getBgmSlider().setValue(val);
        });

        settings.getDarkModeToggleBtn().addActionListener(e -> {
            boolean newState = !settingsModel.isDarkModeEnabled();
            settingsModel.setDarkModeEnabled(newState);
            updateToggleButton(settings.getDarkModeToggleBtn(), newState);
            ThemeManager.applyTheme(mainFrame, newState);
        });
    }

    private void setupSimulationListeners() {
        SimulatorSetupView setup = mainFrame.getSetupView();
        SimulationView simView = mainFrame.getSimulationView();

        // 1. Setup screen Back Button
        setup.getBackBtn().addActionListener(e -> mainFrame.showDashboard());

        // 2. Setup screen Remove Button
        setup.getRemoveRowBtn().addActionListener(e -> {
            DefaultTableModel model = setup.getTableModel();
            int selectedRow = setup.getProcessTable().getSelectedRow();

            if (model.getRowCount() <= 3) {
                JOptionPane.showMessageDialog(mainFrame, "A minimum of 3 processes is required.");
                return;
            }

            if (selectedRow != -1) model.removeRow(selectedRow);
            else model.removeRow(model.getRowCount() - 1); // Remove last if nothing is selected
        });

        // 3. Setup screen Randomize Button
        setup.getRandomizeBtn().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame,
                "Are you sure you want to randomize? This will overwrite the current table data.",
                "Confirm Randomize", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                List<ProcessModel> randProcs = GenerateRandomProcesses.generateRandom();
                DefaultTableModel model = setup.getTableModel();
                model.setRowCount(0); // Clear current table

                // Populate table with generated logic for user to modify
                for (ProcessModel p : randProcs) {
                    model.addRow(new Object[]{p.getProcessId(), p.getBurstTime(), p.getArrivalTime(), p.getPriority()});
                }
            }
        });

        // 4. Proceed to Simulation
        setup.getProceedBtn().addActionListener(e -> {
            if (generateAndValidateData()) {
                mainFrame.showSimulation();
                startSimulation();
            }
        });

        // 5. Load File Action
        setup.getLoadFileBtn().addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                loadProcessesFromFile(file);
            }
        });

        // Simulation Screen Actions
        simView.getNewBatchBtn().addActionListener(e -> {
            stopSimulationTimer();
            mainFrame.showSetup();
        });

        simView.getRestartBtn().addActionListener(e -> {
            stopSimulationTimer();
            resetCurrentProcesses();
            startSimulation();
        });

        simView.getTogglePauseBtn().addActionListener(e -> {
            if (simulationTimer != null) {
                if (simulationTimer.isRunning()) {
                    simulationTimer.stop();
                    simView.getTogglePauseBtn().setText("Play");
                } else {
                    simulationTimer.start();
                    simView.getTogglePauseBtn().setText("Pause");
                }
            }
        });

        simView.getExitBtn().addActionListener(e -> {
            stopSimulationTimer();
            mainFrame.showDashboard();
        });
    }

    private void stopSimulationTimer() {
        if (simulationTimer != null && simulationTimer.isRunning()) simulationTimer.stop();
    }

    private void resetCurrentProcesses() {
        for (ProcessModel p : currentProcesses) {
            p.setRemainingTime(p.getBurstTime());
            p.setCompletionTime(0);
            p.setWaitingTime(0);
            p.setTurnaroundTime(0);
        }
    }

    private void startSimulation() {
        SimulatorSetupView setup = mainFrame.getSetupView();
        SimulationView simView = mainFrame.getSimulationView();

        String algo = (String) setup.getAlgorithmCombo().getSelectedItem();
        String method = (String) setup.getGenerationMethodCombo().getSelectedItem();

        simView.setupSimulation(method, algo, currentProcesses);

        if (algo.equals("First Come First Serve")) {
            currentSimulator = new FCFSSimulator(currentProcesses);
        } else if (algo.equals("Round Robin")) {
            int quantum = (Integer) setup.getQuantumSpinner().getValue();
            currentSimulator = new RoundRobinSimulator(currentProcesses, quantum);
        } else if (algo.equals("Shortest Job First (Preemptive)")) {
            currentSimulator = new SRTSimulator(currentProcesses);
        } else if (algo.equals("Shortest Job First (Non-preemptive)")) {
            currentSimulator = new SJFSimulator(currentProcesses);
        } else if (algo.equals("Priority (Preemptive)")) {
            boolean isLowerBetter = setup.getPriorityOrderCombo().getSelectedIndex() == 0;
            currentSimulator = new PreemptivePrioritySimulator(currentProcesses, isLowerBetter);
        } else if (algo.equals("Priority (Non-preemptive)")) {
            boolean isLowerBetter = setup.getPriorityOrderCombo().getSelectedIndex() == 0;
            currentSimulator = new PrioritySimulator(currentProcesses, isLowerBetter);
        }

        simView.getTogglePauseBtn().setText("Pause");

        // Gantt Chart Frame Loop
        simulationTimer = new Timer(500, e -> {
            boolean hasMore = currentSimulator.executeStep();
            simView.addGanttBlock(currentSimulator.getActiveProcessId(), currentSimulator.getCurrentTime() - 1);

            if (currentSimulator.getActiveProcessId() != null && !currentSimulator.getActiveProcessId().equals("IDLE")) {
                for (ProcessModel p : currentProcesses) {
                    if (p.getProcessId().equals(currentSimulator.getActiveProcessId()) && p.getRemainingTime() == 0) {
                        simView.updateProcessStats(p);
                    }
                }
            }

            if (!hasMore) {
                simulationTimer.stop();
                double totalWt = 0;
                double totalTat = 0;
                for (ProcessModel p : currentProcesses) {
                    totalWt += p.getWaitingTime();
                    totalTat += p.getTurnaroundTime();
                }
                simView.updateAverages(totalWt / currentProcesses.size(), totalTat / currentProcesses.size());
                JOptionPane.showMessageDialog(mainFrame, "Simulation Complete!");
            }
        });
        simulationTimer.start();
    }

    private boolean generateAndValidateData() {
        SimulatorSetupView setup = mainFrame.getSetupView();
        DefaultTableModel model = setup.getTableModel();

        int rowCount = model.getRowCount();
        if (rowCount < 3 || rowCount > 20) {
            JOptionPane.showMessageDialog(mainFrame, "Must be between 3 and 20 processes.");
            return false;
        }

        currentProcesses = new ArrayList<>();
        Set<Integer> priorities = new HashSet<>();

        // We now ALWAYS read from the table, guaranteeing random data is evaluated exactly
        // as the user sees it (including any manual edits they made to the random values).
        for (int i = 0; i < rowCount; i++) {
            try {
                String id = model.getValueAt(i, 0).toString().trim();
                String burstStr = model.getValueAt(i, 1).toString().trim();
                String arrivalStr = model.getValueAt(i, 2).toString().trim();
                String priorityStr = model.getValueAt(i, 3).toString().trim();

                if (burstStr.isEmpty() || arrivalStr.isEmpty() || priorityStr.isEmpty()) {
                    throw new Exception("Fields cannot be left blank.");
                }

                int burst = Integer.parseInt(burstStr);
                int arrival = Integer.parseInt(arrivalStr);
                int priority = Integer.parseInt(priorityStr);

                if (burst < 1 || burst > 30) throw new Exception("Burst time must be 1-30.");
                if (arrival < 0 || arrival > 30) throw new Exception("Arrival time must be 0-30.");
                if (priority < 1 || priority > 20) throw new Exception("Priority must be 1-20.");
                if (!priorities.add(priority)) throw new Exception("Priority duplicate found: " + priority);

                currentProcesses.add(new ProcessModel(id, burst, arrival, priority));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(mainFrame, "Row " + (i + 1) + " invalid: " + ex.getMessage());
                return false;
            }
        }

        return true;
    }

    private void loadProcessesFromFile(File file) {
        DefaultTableModel model = mainFrame.getSetupView().getTableModel();
        model.setRowCount(0);
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine() && model.getRowCount() < 20) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                if (p.length >= 4) model.addRow(new Object[]{p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim()});
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(mainFrame, "Error reading file: " + ex.getMessage());
        }
    }

    private void updateToggleButton(JButton button, boolean isEnabled) {
        if (isEnabled) {
            button.setText("Enabled");
            button.setBackground(SettingsView.ENABLED_GREEN);
        } else {
            button.setText("Disabled");
            button.setBackground(SettingsView.DISABLED_RED);
        }
    }

    private void confirmAndExit() {
        if (JOptionPane.showConfirmDialog(mainFrame, "Exit ChronOS?", "Exit", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void setupWindowExitListener() {
        mainFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) { confirmAndExit(); }
        });
    }

    public void startApplication() {
        Timer transitionTimer = new Timer(3000, e -> mainFrame.showDashboard());
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }
}