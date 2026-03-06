package cmsc125.lab3.views;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SimulatorSetupView extends JPanel {
    private final JComboBox<String> generationMethodCombo;
    private final JComboBox<String> algorithmCombo;
    private final JSpinner quantumSpinner;
    private final JComboBox<String> priorityOrderCombo;

    private final DefaultTableModel tableModel;
    private final JTable processTable;
    private final JButton addRowBtn, removeRowBtn, proceedBtn, loadFileBtn, randomizeBtn, backBtn;

    private final JPanel dynamicOptionsPanel;

    public SimulatorSetupView() {
        setLayout(new BorderLayout(15, 15));

        // --- Fonts for scaling up UI ---
        Font labelFont = new Font("SansSerif", Font.BOLD, 18);
        Font comboFont = new Font("SansSerif", Font.PLAIN, 18);
        Font btnFont = new Font("SansSerif", Font.BOLD, 16);

        // --- TOP CONTAINER ---
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

        // Row 1: Back, Generation, Algorithm
        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        backBtn = new JButton("← Back");
        backBtn.setFont(btnFont);

        generationMethodCombo = new JComboBox<>(new String[]{"User-defined input", "User-defined input from a text file"});
        generationMethodCombo.setFont(comboFont);

        algorithmCombo = new JComboBox<>(new String[]{
            "First Come First Serve", "Round Robin", "Shortest Job First (Preemptive)",
            "Shortest Job First (Non-preemptive)", "Priority (Preemptive)", "Priority (Non-preemptive)"
        });
        algorithmCombo.setFont(comboFont);

        JLabel genLabel = new JLabel("Data Source:");
        genLabel.setFont(labelFont);
        JLabel algoLabel = new JLabel("Algorithm:");
        algoLabel.setFont(labelFont);

        row1.add(backBtn);
        row1.add(Box.createHorizontalStrut(20));
        row1.add(genLabel);
        row1.add(generationMethodCombo);
        row1.add(Box.createHorizontalStrut(20));
        row1.add(algoLabel);
        row1.add(algorithmCombo);

        // Row 2: Dynamic Options & Specific Action Buttons
        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        dynamicOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        quantumSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        quantumSpinner.setFont(comboFont);
        priorityOrderCombo = new JComboBox<>(new String[]{"Lower Number = High Priority", "Higher Number = High Priority"});
        priorityOrderCombo.setFont(comboFont);

        randomizeBtn = new JButton("🎲 Randomize Data");
        randomizeBtn.setFont(btnFont);

        loadFileBtn = new JButton("📁 Load File");
        loadFileBtn.setFont(btnFont);
        loadFileBtn.setVisible(false); // Hidden by default (User input is default)

        row2.add(dynamicOptionsPanel);
        row2.add(randomizeBtn);
        row2.add(loadFileBtn);

        topContainer.add(row1);
        topContainer.add(row2);
        add(topContainer, BorderLayout.NORTH);

        // --- CENTER CONTAINER (Table) ---
        String[] columns = {"Process ID", "Burst Time (1-30)", "Arrival Time (0-30)", "Priority Number (1-20)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Index 0 is "User-defined input", so it is editable. Index 1 is File mode (Read-only).
                return generationMethodCombo.getSelectedIndex() == 0;
            }
        };
        processTable = new JTable(tableModel);
        processTable.setFont(new Font("SansSerif", Font.PLAIN, 16));
        processTable.setRowHeight(35);
        processTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 18));
        add(new JScrollPane(processTable), BorderLayout.CENTER);

        // --- BOTTOM CONTAINER (Actions) ---
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        addRowBtn = new JButton("+ Add Process");
        addRowBtn.setFont(btnFont);

        removeRowBtn = new JButton("- Remove Process");
        removeRowBtn.setFont(btnFont);

        proceedBtn = new JButton("Proceed to Simulation");
        proceedBtn.setFont(new Font("SansSerif", Font.BOLD, 20));
        proceedBtn.setPreferredSize(new Dimension(250, 50));

        bottomPanel.add(addRowBtn);
        bottomPanel.add(removeRowBtn);
        bottomPanel.add(proceedBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        // Initialize Default Table Rows
        for(int i=1; i<=3; i++) tableModel.addRow(new Object[]{"P"+i, "", "", ""});

        setupListeners();
    }

    private void setupListeners() {
        algorithmCombo.addActionListener(e -> {
            dynamicOptionsPanel.removeAll();
            String algo = (String) algorithmCombo.getSelectedItem();
            if (algo.equals("Round Robin")) {
                JLabel qLabel = new JLabel("Quantum (1-10):");
                qLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                dynamicOptionsPanel.add(qLabel);
                dynamicOptionsPanel.add(quantumSpinner);
            } else if (algo.contains("Priority")) {
                JLabel pLabel = new JLabel("Priority Rule:");
                pLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
                dynamicOptionsPanel.add(pLabel);
                dynamicOptionsPanel.add(priorityOrderCombo);
            }
            dynamicOptionsPanel.revalidate();
            dynamicOptionsPanel.repaint();
        });

        generationMethodCombo.addActionListener(e -> {
            int idx = generationMethodCombo.getSelectedIndex();
            tableModel.setRowCount(0); // Clear table

            if (idx == 0) { // User Input Mode
                addRowBtn.setVisible(true);
                removeRowBtn.setVisible(true);
                randomizeBtn.setVisible(true);
                loadFileBtn.setVisible(false);
                // Pre-fill 3 blank rows
                for(int i=1; i<=3; i++) tableModel.addRow(new Object[]{"P"+i, "", "", ""});
            } else { // File Input Mode
                addRowBtn.setVisible(false);
                removeRowBtn.setVisible(false);
                randomizeBtn.setVisible(false);
                loadFileBtn.setVisible(true);
            }
        });

        addRowBtn.addActionListener(e -> {
            if (tableModel.getRowCount() < 20) {
                tableModel.addRow(new Object[]{"P" + (tableModel.getRowCount() + 1), "", "", ""});
            } else JOptionPane.showMessageDialog(this, "Maximum of 20 processes allowed.");
        });
    }

    public JComboBox<String> getGenerationMethodCombo() { return generationMethodCombo; }
    public JComboBox<String> getAlgorithmCombo() { return algorithmCombo; }
    public JSpinner getQuantumSpinner() { return quantumSpinner; }
    public JComboBox<String> getPriorityOrderCombo() { return priorityOrderCombo; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JTable getProcessTable() { return processTable; }
    public JButton getAddRowBtn() { return addRowBtn; }
    public JButton getRemoveRowBtn() { return removeRowBtn; }
    public JButton getProceedBtn() { return proceedBtn; }
    public JButton getLoadFileBtn() { return loadFileBtn; }
    public JButton getRandomizeBtn() { return randomizeBtn; }
    public JButton getBackBtn() { return backBtn; }
}