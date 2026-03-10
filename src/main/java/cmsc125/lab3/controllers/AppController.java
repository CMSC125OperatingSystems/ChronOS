package cmsc125.lab3.controllers;

import cmsc125.lab3.models.SettingsModel;
import cmsc125.lab3.models.ProcessModel;
import cmsc125.lab3.services.*;
import cmsc125.lab3.views.*;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;

public class AppController {
    private final MainFrame mainFrame;
    private final AudioService audioService;
    private final SettingsModel settingsModel;

    private Timer simulationTimer;
    private BaseSimulator currentSimulator;
    private List<ProcessModel> currentProcesses;

    // Base simulation speed = 500ms per tick (1.0x)
    private static final int BASE_DELAY_MS = 500;

    public AppController(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        this.audioService = new AudioService();
        this.settingsModel = new SettingsModel();

        syncSettingsViewWithModel();

        mainFrame.getDashboardView().getPlayButton().addActionListener(e -> mainFrame.showSetup());
        mainFrame.getDashboardView().getSettingsButton().addActionListener(e -> mainFrame.showSettings());
        mainFrame.getDashboardView().getExitButton().addActionListener(e -> confirmAndExit());
        mainFrame.getSettingsView().getBackBtn().addActionListener(e -> mainFrame.showDashboard());
        mainFrame.getDashboardView().getHowToPlayButton().addActionListener(e -> mainFrame.showHelp());
        mainFrame.getHelpView().getBackBtn().addActionListener(e -> mainFrame.showDashboard());

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

        settings.getBgmToggleBtn().addActionListener(e -> {
            boolean newState = !settingsModel.isBgmEnabled();
            settingsModel.setBgmEnabled(newState);
            updateToggleButton(settings.getBgmToggleBtn(), newState);

            audioService.updateBgmVolume(settingsModel.getBgmVolume(), newState);
        });

        settings.getBgmSlider().addChangeListener(e -> {
            int val = settings.getBgmSlider().getValue();
            settingsModel.setBgmVolume(val);
            settings.getBgmSpinner().setValue(val);

            audioService.updateBgmVolume(val, settingsModel.isBgmEnabled());
        });

        settings.getBgmSpinner().addChangeListener(e -> {
            int val = (Integer) settings.getBgmSpinner().getValue();
            settingsModel.setBgmVolume(val);
            settings.getBgmSlider().setValue(val);

            audioService.updateBgmVolume(val, settingsModel.isBgmEnabled());
        });

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

        setup.getBackBtn().addActionListener(e -> mainFrame.showDashboard());

        setup.getRemoveRowBtn().addActionListener(e -> {
            DefaultTableModel model = setup.getTableModel();
            JTable table = setup.getProcessTable();
            int[] selectedRows = table.getSelectedRows();

            int rowsToDelete = (selectedRows.length > 0) ? selectedRows.length : 1;
            int remainingRows = model.getRowCount() - rowsToDelete;

            if (remainingRows < 3) {
                JOptionPane.showMessageDialog(mainFrame, "A minimum of 3 processes is required.");
                return;
            }

            if (selectedRows.length > 0) {
                for (int i = selectedRows.length - 1; i >= 0; i--) {
                    model.removeRow(selectedRows[i]);
                }
            } else {
                model.removeRow(model.getRowCount() - 1);
            }

            renumberProcessIDs(model);
        });

        setup.getAddRowBtn().addActionListener(e -> {
            DefaultTableModel model = setup.getTableModel();
            if (model.getRowCount() < 20) {
                model.addRow(new Object[]{"", "", "", ""});
                renumberProcessIDs(model);
            } else {
                JOptionPane.showMessageDialog(mainFrame, "Maximum of 20 processes allowed.");
            }
        });

        // Randomize entries
        setup.getRandomizeBtn().addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(mainFrame, "Randomize data? This overwrites current inputs.", "Confirm", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                DefaultTableModel model = setup.getTableModel();
                int currentRowCount = model.getRowCount();

                List<ProcessModel> randProcs = GenerateRandomProcesses.generateRandom(currentRowCount);

                for (int i = 0; i < currentRowCount; i++) {
                    ProcessModel p = randProcs.get(i);
                    model.setValueAt("P" + (i + 1), i, 0);
                    model.setValueAt(p.getBurstTime(), i, 1);
                    model.setValueAt(p.getArrivalTime(), i, 2);
                    model.setValueAt(p.getPriority(), i, 3);
                }
            }
        });

        // Proceed processing
        setup.getProceedBtn().addActionListener(e -> {
            if (generateAndValidateData()) {
                mainFrame.showSimulation();
                startSimulation();
            }
        });

        // Load file
        setup.getLoadFileBtn().addActionListener(e -> {
            // Pass the current working directory to the constructor
            JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));

            // Optional: Add a file filter so it only shows specific files (like .txt or .csv)
             FileNameExtensionFilter filter = new FileNameExtensionFilter("Text Files", "txt");
             fileChooser.setFileFilter(filter);

            if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                loadProcessesFromFile(file);
            }
        });

        // Simulation controls
        simView.getNewBatchBtn().addActionListener(e -> {
            boolean isFinished = simView.getTogglePauseBtn().getText().equals("Finished");

            if (!isFinished) {
                boolean wasRunning = simulationTimer != null && simulationTimer.isRunning();
                if (wasRunning) simulationTimer.stop();

                int confirm = JOptionPane.showConfirmDialog(mainFrame, "The simulation is currently running. Are you sure you want to terminate it and go back to Simulation Setup?", "Confirm Termination", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    stopSimulationTimer();
                    mainFrame.showSetup();
                } else if (wasRunning) simulationTimer.start();
            } else {
                stopSimulationTimer();
                mainFrame.showSetup();
            }
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
                    simView.getTogglePauseBtn().setText("Resume");
                } else {
                    simulationTimer.start();
                    simView.getTogglePauseBtn().setText("Pause");
                }
            }
        });

        simView.getSpeedCombo().addActionListener(e -> {
            if (simulationTimer != null) simulationTimer.setDelay(calculateDelayFromCombo());
        });

        simView.getExitBtn().addActionListener(e -> {
            boolean isFinished = simView.getTogglePauseBtn().getText().equals("Finished");

            if (!isFinished) {
                boolean wasRunning = simulationTimer != null && simulationTimer.isRunning();
                if (wasRunning) simulationTimer.stop();

                int confirm = JOptionPane.showConfirmDialog(mainFrame, "The simulation is currently running. Are you sure you want to terminate it and exit to the Dashboard?", "Confirm Termination", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

                if (confirm == JOptionPane.YES_OPTION) {
                    stopSimulationTimer();
                    mainFrame.showDashboard();
                } else if (wasRunning) simulationTimer.start();
            } else {
                stopSimulationTimer();
                mainFrame.showDashboard();
            }
        });

        setup.getTableModel().addTableModelListener(e -> {
            if (e.getType() == javax.swing.event.TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col < 1) return;

                DefaultTableModel model = setup.getTableModel();
                String valStr = model.getValueAt(row, col).toString().trim();
                if (valStr.isEmpty()) return;

                try {
                    int val = Integer.parseInt(valStr);
                    String error = null;

                    String algo = (String) setup.getAlgorithmCombo().getSelectedItem();
                    boolean isPriorityAlgo = algo != null && algo.toLowerCase().contains("priority");

                    if (col == 1 && (val < 1 || val > 30)) error = "Burst Time must be 1-30.";
                    else if (col == 2 && (val < 0 || val > 30)) error = "Arrival Time must be 0-30.";
                    else if (col == 3 && isPriorityAlgo && (val < 1 || val > 20)) error = "Priority must be 1-20.";

                    if (error != null) {
                        JOptionPane.showMessageDialog(mainFrame, error, "Input Error", JOptionPane.ERROR_MESSAGE);
                        SwingUtilities.invokeLater(() -> model.setValueAt("", row, col));
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "Please enter a valid number.", "Input Error", JOptionPane.ERROR_MESSAGE);
                    SwingUtilities.invokeLater(() -> model.setValueAt("", row, col));
                }
            }
        });
    }

    private void stopSimulationTimer() {
        if (simulationTimer != null && simulationTimer.isRunning()) simulationTimer.stop();
    }

    // Reset process for restart
    private void resetCurrentProcesses() {
        for (ProcessModel p : currentProcesses) {
            p.setRemainingTime(p.getBurstTime());
            p.setCompletionTime(0);
            p.setWaitingTime(0);
            p.setTurnaroundTime(0);
        }
    }

    // Calculate delay
    private int calculateDelayFromCombo() {
        String selected = (String) mainFrame.getSimulationView().getSpeedCombo().getSelectedItem();
        double multiplier = Double.parseDouble(selected.replace("x", ""));
        return (int) (BASE_DELAY_MS / multiplier);
    }

    private void startSimulation() {
        SimulatorSetupView setup = mainFrame.getSetupView();
        SimulationView simView = mainFrame.getSimulationView();

        String algo = (String) setup.getAlgorithmCombo().getSelectedItem();
        String method = (String) setup.getGenerationMethodCombo().getSelectedItem();

        simView.setupSimulation(method, algo, currentProcesses);

        switch (Objects.requireNonNull(algo)) {
            case "First Come First Serve" -> currentSimulator = new FCFSSimulator(currentProcesses);
            case "Round Robin" -> {
                int quantum = (Integer) setup.getQuantumSpinner().getValue();
                currentSimulator = new RoundRobinSimulator(currentProcesses, quantum);
            }
            case "Shortest Job First (Preemptive)" -> currentSimulator = new SRTSimulator(currentProcesses);
            case "Shortest Job First (Non-preemptive)" -> currentSimulator = new SJFSimulator(currentProcesses);
            case "Priority (Preemptive)" -> {
                boolean isLowerBetter = setup.getPriorityOrderCombo().getSelectedIndex() == 0;
                currentSimulator = new PreemptivePrioritySimulator(currentProcesses, isLowerBetter);
            }
            case "Priority (Non-preemptive)" -> {
                boolean isLowerBetter = setup.getPriorityOrderCombo().getSelectedIndex() == 0;
                currentSimulator = new PrioritySimulator(currentProcesses, isLowerBetter);
            }
        }

        simView.getTogglePauseBtn().setText("Pause");
        simView.getTogglePauseBtn().setEnabled(true);

        simulationTimer = new Timer(calculateDelayFromCombo(), e -> {
            boolean hasMoreSteps = currentSimulator.executeStep();

            if (!hasMoreSteps) {
                simulationTimer.stop();
                simView.getTogglePauseBtn().setText("Finished");
                simView.getTogglePauseBtn().setEnabled(false);
                calculateAndDisplayAverages();
                JOptionPane.showMessageDialog(mainFrame, "Simulation Complete!");
                return;
            }

            simView.addGanttBlock(currentSimulator.getActiveProcessId(), currentSimulator.getCurrentTime() - 1);

            if (currentSimulator.getActiveProcessId() != null && !currentSimulator.getActiveProcessId().equals("IDLE")) {
                for (ProcessModel p : currentProcesses) {
                    if (p.getProcessId().equals(currentSimulator.getActiveProcessId()) && p.getRemainingTime() == 0) simView.updateProcessStats(p);
                }
            }
        });

        simulationTimer.start();
    }

    private void calculateAndDisplayAverages() {
        double totalWt = 0;
        double totalTat = 0;
        for (ProcessModel p : currentProcesses) {
            totalWt += p.getWaitingTime();
            totalTat += p.getTurnaroundTime();
        }
        mainFrame.getSimulationView().updateAverages(totalWt / currentProcesses.size(), totalTat / currentProcesses.size());
    }

    private boolean generateAndValidateData() {
        SimulatorSetupView setup = mainFrame.getSetupView();

        JTable table = setup.getProcessTable();
        if (table.isEditing()) table.getCellEditor().stopCellEditing();

        DefaultTableModel model = setup.getTableModel();
        int rowCount = model.getRowCount();
        if (rowCount < 3 || rowCount > 20) {
            JOptionPane.showMessageDialog(mainFrame, "Must be between 3 and 20 processes.");
            return false;
        }

        currentProcesses = new ArrayList<>();
        Set<Integer> priorities = new HashSet<>();

        for (int i = 0; i < rowCount; i++) {
            try {
                String id = model.getValueAt(i, 0).toString().trim();
                String burstStr = model.getValueAt(i, 1).toString().trim();
                String arrivalStr = model.getValueAt(i, 2).toString().trim();
                String priorityStr = model.getValueAt(i, 3).toString().trim();

                if (burstStr.isEmpty() || arrivalStr.isEmpty() || priorityStr.isEmpty()) throw new Exception("Fields cannot be left blank.");

                int burst = Integer.parseInt(burstStr);
                int arrival = Integer.parseInt(arrivalStr);
                String algo = (String) setup.getAlgorithmCombo().getSelectedItem();
                boolean isPriorityAlgo = algo != null && algo.toLowerCase().contains("priority");
                int priority = 1;

                if (!priorityStr.isEmpty()) priority = Integer.parseInt(priorityStr);

                if (burst < 1 || burst > 30) throw new Exception("Burst time must be 1-30.");
                if (arrival < 0 || arrival > 30) throw new Exception("Arrival time must be 0-30.");


                if (isPriorityAlgo) {
                    if (priority < 1 || priority > 20) throw new Exception("Priority must be 1-20.");
                    if (!priorities.add(priority)) throw new Exception("Priority duplicate found: " + priority);
                }

                currentProcesses.add(new ProcessModel(id, burst, arrival, priority));

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

    // To remember process IDs
    private void renumberProcessIDs(DefaultTableModel model) {
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt("P" + (i + 1), i, 0);
        }
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
            settingsModel.saveSettings();
            audioService.stopBGM();
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
        audioService.playSFX("/audio/opening.wav", settingsModel.getSfxVolume(), settingsModel.isSfxEnabled());
        Timer transitionTimer = new Timer(3000, e -> {
            mainFrame.showDashboard();
            audioService.playBGM("/audio/bgm.wav", settingsModel.getBgmVolume(), settingsModel.isBgmEnabled());
        });
        transitionTimer.setRepeats(false);
        transitionTimer.start();
    }
}