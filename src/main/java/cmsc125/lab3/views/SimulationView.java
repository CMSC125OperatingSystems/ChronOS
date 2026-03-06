package cmsc125.lab3.views;

import cmsc125.lab3.models.ProcessModel;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class SimulationView extends JPanel {
    private final JLabel infoLabel, timeLabel, avgWtLabel, avgTatLabel;
    private final DefaultTableModel resultTableModel;
    private final JPanel ganttPanel;
    private final JButton newBatchBtn, restartBtn, togglePauseBtn, exitBtn;

    private final Map<String, Color> processColors = new HashMap<>();
    private final Color[] palette = {
        Color.RED, Color.BLUE, new Color(0, 150, 0), Color.ORANGE, Color.MAGENTA, Color.CYAN,
        Color.PINK, new Color(150, 150, 0), Color.DARK_GRAY, new Color(128, 0, 128)
    };

    public SimulationView() {
        setLayout(new BorderLayout(10, 10));

        infoLabel = new JLabel("Method | Algorithm");
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        String[] cols = {"Process ID", "Burst Time", "Arrival Time", "Priority", "Waiting Time", "Turnaround Time"};
        resultTableModel = new DefaultTableModel(cols, 0);
        centerPanel.add(new JScrollPane(new JTable(resultTableModel)), BorderLayout.CENTER);

        JPanel avgPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 10));
        avgWtLabel = new JLabel("Average Waiting Time: 0.00");
        avgTatLabel = new JLabel("Average Turnaround Time: 0.00");
        avgPanel.add(avgWtLabel);
        avgPanel.add(avgTatLabel);
        centerPanel.add(avgPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel ganttContainer = new JPanel(new BorderLayout());

        timeLabel = new JLabel("Time: 00:00");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ganttContainer.add(timeLabel, BorderLayout.NORTH);

        ganttPanel = new JPanel();
        ganttPanel.setLayout(new BoxLayout(ganttPanel, BoxLayout.X_AXIS));
        ganttPanel.setBackground(Color.WHITE);
        JScrollPane scrollPane = new JScrollPane(ganttPanel);
        scrollPane.setPreferredSize(new Dimension(800, 70));
        ganttContainer.add(scrollPane, BorderLayout.CENTER);
        bottomPanel.add(ganttContainer, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        newBatchBtn = new JButton("Process New Batch");
        restartBtn = new JButton("Restart");
        togglePauseBtn = new JButton("Pause");
        exitBtn = new JButton("Exit");

        controlsPanel.add(newBatchBtn); controlsPanel.add(restartBtn);
        controlsPanel.add(togglePauseBtn); controlsPanel.add(exitBtn);

        bottomPanel.add(controlsPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setupSimulation(String method, String algorithm, List<ProcessModel> processes) {
        infoLabel.setText(method + "   |   " + algorithm);
        resultTableModel.setRowCount(0);
        processColors.clear();
        int cIdx = 0;

        for (ProcessModel p : processes) {
            resultTableModel.addRow(new Object[]{ p.getProcessId(), p.getBurstTime(), p.getArrivalTime(), p.getPriority(), "", "" });
            processColors.put(p.getProcessId(), palette[cIdx % palette.length]);
            cIdx++;
        }

        ganttPanel.removeAll();
        ganttPanel.revalidate();
        ganttPanel.repaint();
        timeLabel.setText("Time: 00:00");
        avgWtLabel.setText("Average Waiting Time: 0.00");
        avgTatLabel.setText("Average Turnaround Time: 0.00");
    }

    public void addGanttBlock(String processId, int time) {
        timeLabel.setText(String.format("Time: %02d:%02d", time / 60, time % 60));
        JLabel block = new JLabel(processId == null || processId.equals("IDLE") ? "-" : processId);
        block.setOpaque(true);
        block.setPreferredSize(new Dimension(45, 50));
        block.setHorizontalAlignment(SwingConstants.CENTER);
        block.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        if (processId == null || processId.equals("IDLE")) {
            block.setBackground(Color.WHITE); block.setForeground(Color.BLACK);
        } else {
            block.setBackground(processColors.getOrDefault(processId, Color.GRAY));
            block.setForeground(Color.WHITE);
        }

        ganttPanel.add(block);
        ganttPanel.revalidate();
        Container parent = ganttPanel.getParent();
        if (parent instanceof JViewport) {
            Rectangle bounds = ganttPanel.getBounds();
            bounds.setLocation(bounds.width - ((JViewport) parent).getWidth(), 0);
            ganttPanel.scrollRectToVisible(bounds);
        }
    }

    public void updateProcessStats(ProcessModel p) {
        for (int i = 0; i < resultTableModel.getRowCount(); i++) {
            if (resultTableModel.getValueAt(i, 0).equals(p.getProcessId())) {
                resultTableModel.setValueAt(p.getWaitingTime(), i, 4);
                resultTableModel.setValueAt(p.getTurnaroundTime(), i, 5);
                break;
            }
        }
    }

    public void updateAverages(double avgWt, double avgTat) {
        avgWtLabel.setText(String.format("Average Waiting Time: %.2f", avgWt));
        avgTatLabel.setText(String.format("Average Turnaround Time: %.2f", avgTat));
    }

    public JButton getNewBatchBtn() { return newBatchBtn; }
    public JButton getRestartBtn() { return restartBtn; }
    public JButton getTogglePauseBtn() { return togglePauseBtn; }
    public JButton getExitBtn() { return exitBtn; }
}