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
    private final JLabel quantumLabel;
    private final JLabel ruleLabel;

    private final JPanel dynamicOptionsPanel;

    public SimulatorSetupView() {
        setLayout(new BorderLayout(15, 15));

        Font comboFont = new Font("SansSerif", Font.PLAIN, 18);
        Font btnFont = new Font("SansSerif", Font.BOLD, 16);

        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));

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

        row1.add(backBtn);
        row1.add(new JLabel("Method:"));
        row1.add(generationMethodCombo);
        row1.add(new JLabel("Algorithm:"));
        row1.add(algorithmCombo);

        quantumLabel = new JLabel("Quantum (1-10):");
        ruleLabel = new JLabel("Rule:");

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        dynamicOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));

        quantumSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 10, 1));
        quantumSpinner.setFont(comboFont);
        // Access the editor component of the spinner
        JComponent editor = quantumSpinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            // Cast to DefaultEditor to get the text field
            JTextField textField = ((JSpinner.DefaultEditor) editor).getTextField();
            // Disable direct keyboard input
            textField.setEditable(false);
            textField.setFocusable(false);
            // Optional: Keep the background color consistent with your theme
            textField.setBackground(UIManager.getColor("Panel.background"));
        }

        priorityOrderCombo = new JComboBox<>(new String[]{"Lower Number = High Priority", "Higher Number = High Priority"});
        priorityOrderCombo.setFont(comboFont);

        randomizeBtn = new JButton("Randomize Data");
        randomizeBtn.setFont(btnFont);

        loadFileBtn = new JButton("Load File");
        loadFileBtn.setFont(btnFont);
        loadFileBtn.setVisible(false);

        row2.add(dynamicOptionsPanel);
        row2.add(randomizeBtn);
        row2.add(loadFileBtn);

        topContainer.add(row1);
        topContainer.add(row2);
        add(topContainer, BorderLayout.NORTH);

        String[] columns = {"Process ID", "Burst Time (1-30)", "Arrival Time (0-30)", "Priority (1-20)"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return generationMethodCombo.getSelectedIndex() == 0 && column != 0;
            }
        };
        processTable = new JTable(tableModel);
        processTable.setRowHeight(30);
        add(new JScrollPane(processTable), BorderLayout.CENTER);
        add(Box.createHorizontalStrut(50), BorderLayout.WEST); // 50px left margin
        add(Box.createHorizontalStrut(50), BorderLayout.EAST); // 50px right margin

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 15));
        addRowBtn = new JButton("+ Add Process");
        removeRowBtn = new JButton("- Remove Process");
        proceedBtn = new JButton("Proceed to Simulation");
        proceedBtn.setFont(new Font("SansSerif", Font.BOLD, 20));

        bottomPanel.add(addRowBtn);
        bottomPanel.add(removeRowBtn);
        bottomPanel.add(proceedBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        for (int i = 1; i <= 3; i++) tableModel.addRow(new Object[]{"P" + i, "", "", ""});
        setupListeners();
    }

    private void setupListeners() {
        algorithmCombo.addActionListener(e -> {
            dynamicOptionsPanel.removeAll();
            String algo = (String) algorithmCombo.getSelectedItem();

            if (algo.equals("Round Robin")) {
                JLabel quantumLabel = new JLabel("Quantum (1-10):");

                // MANUALLY SYNC: Copy the font and color from the Combo Box itself
                quantumLabel.setFont(algorithmCombo.getFont());
                quantumLabel.setForeground(algorithmCombo.getForeground());

                dynamicOptionsPanel.add(quantumLabel);
                dynamicOptionsPanel.add(quantumSpinner);
            } else if (algo.contains("Priority")) {
                JLabel ruleLabel = new JLabel("Rule:");

                // MANUALLY SYNC: Copy the font and color from the Combo Box itself
                ruleLabel.setFont(algorithmCombo.getFont());
                ruleLabel.setForeground(algorithmCombo.getForeground());

                dynamicOptionsPanel.add(ruleLabel);
                dynamicOptionsPanel.add(priorityOrderCombo);
            }

            dynamicOptionsPanel.revalidate();
            dynamicOptionsPanel.repaint();
        });
        generationMethodCombo.addActionListener(e -> {
            int idx = generationMethodCombo.getSelectedIndex();
            tableModel.setRowCount(0);

            if (idx == 0) { // User Input
                addRowBtn.setVisible(true);
                removeRowBtn.setVisible(true);
                randomizeBtn.setVisible(true);
                loadFileBtn.setVisible(false);
                for(int i=1; i<=3; i++) tableModel.addRow(new Object[]{"P"+i, "", "", ""});
            } else {
                addRowBtn.setVisible(false);
                removeRowBtn.setVisible(false);
                randomizeBtn.setVisible(false);
                loadFileBtn.setVisible(true);
            }
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
