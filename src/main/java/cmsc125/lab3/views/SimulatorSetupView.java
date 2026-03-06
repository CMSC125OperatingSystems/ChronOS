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
    private final JButton addRowBtn, proceedBtn, loadFileBtn;

    private final JPanel dynamicOptionsPanel;

    public SimulatorSetupView() {
        setLayout(new BorderLayout(10, 10));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));

        generationMethodCombo = new JComboBox<>(new String[]{"Random", "User-defined input", "User-defined input from a text file"});
        algorithmCombo = new JComboBox<>(new String[]{
            "First Come First Serve", "Round Robin", "Shortest Job First (Preemptive)",
            "Shortest Job First (Non-preemptive)", "Priority (Preemptive)", "Priority (Non-preemptive)"
        });

        topPanel.add(new JLabel("Data Generation:"));
        topPanel.add(generationMethodCombo);
        topPanel.add(new JLabel("Algorithm:"));
        topPanel.add(algorithmCombo);

        dynamicOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        quantumSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        priorityOrderCombo = new JComboBox<>(new String[]{"Lower Number = High Priority", "Higher Number = High Priority"});

        topPanel.add(dynamicOptionsPanel);

        loadFileBtn = new JButton("Load File");
        loadFileBtn.setVisible(false);
        topPanel.add(loadFileBtn);

        add(topPanel, BorderLayout.NORTH);

        String[] columns = {"Process ID", "Burst Time (1-30)", "Arrival Time (0-30)", "Priority Number (1-20)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return generationMethodCombo.getSelectedIndex() == 1; // Only editable if user-defined
            }
        };
        processTable = new JTable(tableModel);
        processTable.setRowHeight(25);
        add(new JScrollPane(processTable), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        addRowBtn = new JButton("+ Add Process");
        addRowBtn.setVisible(false);
        proceedBtn = new JButton("Proceed");

        bottomPanel.add(addRowBtn);
        bottomPanel.add(proceedBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        setupListeners();
    }

    private void setupListeners() {
        algorithmCombo.addActionListener(e -> {
            dynamicOptionsPanel.removeAll();
            String algo = (String) algorithmCombo.getSelectedItem();
            if (algo.equals("Round Robin")) {
                dynamicOptionsPanel.add(new JLabel("Quantum (1-10):"));
                dynamicOptionsPanel.add(quantumSpinner);
            } else if (algo.contains("Priority")) {
                dynamicOptionsPanel.add(new JLabel("Priority Rule:"));
                dynamicOptionsPanel.add(priorityOrderCombo);
            }
            dynamicOptionsPanel.revalidate();
            dynamicOptionsPanel.repaint();
        });

        generationMethodCombo.addActionListener(e -> {
            int idx = generationMethodCombo.getSelectedIndex();
            tableModel.setRowCount(0);
            if (idx == 0) { // Random
                addRowBtn.setVisible(false);
                loadFileBtn.setVisible(false);
            } else if (idx == 1) { // User Input
                addRowBtn.setVisible(true);
                loadFileBtn.setVisible(false);
                for(int i=1; i<=3; i++) tableModel.addRow(new Object[]{"P"+i, "", "", ""});
            } else { // File
                addRowBtn.setVisible(false);
                loadFileBtn.setVisible(true);
            }
        });

        addRowBtn.addActionListener(e -> {
            if (tableModel.getRowCount() < 20) {
                tableModel.addRow(new Object[]{"P" + (tableModel.getRowCount() + 1), "", "", ""});
            } else JOptionPane.showMessageDialog(this, "Max 20 processes allowed.");
        });
    }

    public JComboBox<String> getGenerationMethodCombo() { return generationMethodCombo; }
    public JComboBox<String> getAlgorithmCombo() { return algorithmCombo; }
    public JSpinner getQuantumSpinner() { return quantumSpinner; }
    public JComboBox<String> getPriorityOrderCombo() { return priorityOrderCombo; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JButton getProceedBtn() { return proceedBtn; }
    public JButton getLoadFileBtn() { return loadFileBtn; }
}