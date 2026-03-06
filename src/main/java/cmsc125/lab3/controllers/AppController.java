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

        mainFrame.getDashboardView().getPlayButton().addActionListener(e -> mainFrame.showSetup());
        mainFrame.getDashboardView().getSettingsButton().addActionListener(e -> mainFrame.showSettings());
        mainFrame.getDashboardView().getExitButton().addActionListener(e -> confirmAndExit());

        mainFrame.getSettingsView().getBackBtn().addActionListener(e -> mainFrame.showDashboard());

        setupSimulationListeners();
        setupWindowExitListener();
    }

    private void setupSimulationListeners() {
        SimulatorSetupView setup = mainFrame.getSetupView();
        SimulationView simView = mainFrame.getSimulationView();

        setup.getProceedBtn().addActionListener(e -> {
            if (generateAndValidateData()) {
                mainFrame.showSimulation();
                startSimulation();
            }
        });

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

        setup.getLoadFileBtn().addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                loadProcessesFromFile(file);
            }
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

        // MAP TO NEW CLASSES HERE
        if (algo.equals("First Come First Serve")) {
            currentSimulator = new FCFSSimulator(currentProcesses);
        } else if (algo.equals("Round Robin")) {
            int quantum = (Integer) setup.getQuantumSpinner().getValue();
            currentSimulator = new RoundRobinSimulator(currentProcesses, quantum);
        } else if (algo.equals("Shortest Job First (Preemptive)")) {
            currentSimulator = new SRTSimulator(currentProcesses); // Map to SRT!
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
        int methodIdx = setup.getGenerationMethodCombo().getSelectedIndex();

        if (methodIdx == 0) {
            currentProcesses = GenerateRandomProcesses.generateRandom();
        } else {
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
                    String id = model.getValueAt(i, 0).toString();
                    int burst = Integer.parseInt(model.getValueAt(i, 1).toString());
                    int arrival = Integer.parseInt(model.getValueAt(i, 2).toString());
                    int priority = Integer.parseInt(model.getValueAt(i, 3).toString());

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