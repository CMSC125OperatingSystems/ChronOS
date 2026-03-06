package cmsc125.lab3.views;

import cmsc125.lab3.models.ProcessModel;
import cmsc125.lab3.services.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationView extends JPanel {
    private final JLabel infoLabel, timeLabel, avgWtLabel, avgTatLabel;
    private final DefaultTableModel resultTableModel;
    private final GanttChartPanel ganttChartPanel;
    private final JButton newBatchBtn, restartBtn, togglePauseBtn, exitBtn;

    private final Map<String, Color> processColors = new HashMap<>();
    private final Color[] palette = {
        Color.RED, Color.BLUE, new Color(0, 150, 0), Color.ORANGE, Color.MAGENTA, Color.CYAN,
        new Color(255, 105, 180), new Color(150, 150, 0), new Color(128, 0, 128), new Color(0, 128, 128)
    };

    public SimulationView() {
        setLayout(new BorderLayout(15, 15));

        infoLabel = new JLabel("Method | Algorithm");
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 28)); // Much larger
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        String[] cols = {"Process ID", "Burst Time", "Arrival Time", "Priority", "Waiting Time", "Turnaround Time"};
        resultTableModel = new DefaultTableModel(cols, 0);

        JTable resultTable = new JTable(resultTableModel);
        resultTable.setFont(new Font("SansSerif", Font.PLAIN, 18)); // Larger Table Text
        resultTable.setRowHeight(35); // Taller rows
        resultTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 18)); // Larger Headers
        centerPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);

        JPanel avgPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 10));
        avgWtLabel = new JLabel("Average Waiting Time: 0.00");
        avgTatLabel = new JLabel("Average Turnaround Time: 0.00");
        avgWtLabel.setFont(new Font("SansSerif", Font.BOLD, 20)); // Larger
        avgTatLabel.setFont(new Font("SansSerif", Font.BOLD, 20)); // Larger
        avgPanel.add(avgWtLabel);
        avgPanel.add(avgTatLabel);
        centerPanel.add(avgPanel, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        // Setup Dynamic Gantt Layout
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JPanel ganttContainer = new JPanel(new BorderLayout());
        ganttContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        timeLabel = new JLabel("Time: 00:00");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 24)); // Larger
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ganttContainer.add(timeLabel, BorderLayout.NORTH);

        ganttChartPanel = new GanttChartPanel();
        ganttChartPanel.setPreferredSize(new Dimension(800, 120)); // Big fixed height for bar
        ganttContainer.add(ganttChartPanel, BorderLayout.CENTER);
        bottomPanel.add(ganttContainer, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        newBatchBtn = new JButton("Process New Batch");
        restartBtn = new JButton("Restart");
        togglePauseBtn = new JButton("Pause");
        exitBtn = new JButton("Exit");

        Font btnFont = new Font("SansSerif", Font.BOLD, 18);
        newBatchBtn.setFont(btnFont); restartBtn.setFont(btnFont);
        togglePauseBtn.setFont(btnFont); exitBtn.setFont(btnFont);

        controlsPanel.add(newBatchBtn); controlsPanel.add(restartBtn);
        controlsPanel.add(togglePauseBtn); controlsPanel.add(exitBtn);

        bottomPanel.add(controlsPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    public void setupSimulation(String method, String algorithm, List<ProcessModel> processes) {
        infoLabel.setText(method + "   |   " + algorithm);
        resultTableModel.setRowCount(0);
        processColors.clear();
        ganttChartPanel.reset(); // Reset Bar graphics

        int cIdx = 0;
        for (ProcessModel p : processes) {
            resultTableModel.addRow(new Object[]{ p.getProcessId(), p.getBurstTime(), p.getArrivalTime(), p.getPriority(), "", "" });
            processColors.put(p.getProcessId(), palette[cIdx % palette.length]);
            cIdx++;
        }

        timeLabel.setText("Time: 00:00");
        avgWtLabel.setText("Average Waiting Time: 0.00");
        avgTatLabel.setText("Average Turnaround Time: 0.00");
    }

    public void addGanttBlock(String processId, int tick) {
        Color color = (processId == null || processId.equals("IDLE")) ?
            (ThemeManager.isDarkTheme ? Color.DARK_GRAY : Color.LIGHT_GRAY) :
            processColors.getOrDefault(processId, Color.GRAY);

        ganttChartPanel.addTick(processId == null ? "IDLE" : processId, tick, color);
        timeLabel.setText(String.format("Time: %02d:%02d", (tick+1) / 60, (tick+1) % 60));
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

    // --- INNER CLASS: Custom Fully Responsive Dynamic Gantt Chart Component ---
    class GanttChartPanel extends JPanel {
        class GanttBlock {
            String processId;
            int startTick, endTick;
            Color color;
            public GanttBlock(String p, int s, int e, Color c) {
                this.processId = p; this.startTick = s; this.endTick = e; this.color = c;
            }
        }

        List<GanttBlock> blocks = new ArrayList<>();
        int totalTime = 0;

        public void addTick(String processId, int tick, Color color) {
            if (blocks.isEmpty()) {
                blocks.add(new GanttBlock(processId, tick, tick + 1, color));
            } else {
                GanttBlock last = blocks.get(blocks.size() - 1);
                // Dynamically merge contiguous blocks of same Process
                if (last.processId.equals(processId) && last.endTick == tick) {
                    last.endTick = tick + 1;
                } else {
                    blocks.add(new GanttBlock(processId, tick, tick + 1, color));
                }
            }
            totalTime = tick + 1;
            repaint();
        }

        public void reset() {
            blocks.clear();
            totalTime = 0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (blocks.isEmpty() || totalTime == 0) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight() - 35; // Leave space underneath for the numeric time labels
            int y = 5;

            g2.setFont(new Font("SansSerif", Font.BOLD, 24));
            FontMetrics fm = g2.getFontMetrics();

            for (int i = 0; i < blocks.size(); i++) {
                GanttBlock b = blocks.get(i);
                int x = (int) ((double) b.startTick / totalTime * width);
                int w = (int) ((double) (b.endTick - b.startTick) / totalTime * width);
                if (i == blocks.size() - 1) w = width - x; // Fill remainder exactly to avoid 1px rounding gap

                // 1. Fill Solid Block Color
                g2.setColor(b.color);
                g2.fillRect(x, y, w, height);

                // 2. Outline Block border
                g2.setColor(ThemeManager.isDarkTheme ? Color.WHITE : Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(x, y, w, height);

                // 3. Process ID text inside block
                String text = b.processId.equals("IDLE") ? "-" : b.processId;
                g2.setColor(b.processId.equals("IDLE") ? (ThemeManager.isDarkTheme ? Color.WHITE : Color.BLACK) : Color.WHITE);
                int tx = x + (w - fm.stringWidth(text)) / 2;
                int ty = y + ((height - fm.getHeight()) / 2) + fm.getAscent();

                // Only render text if the segment box is physically wide enough to hold it
                if (w > fm.stringWidth(text) + 10) {
                    g2.drawString(text, tx, ty);
                }

                // 4. Numeric time guidelines beneath the block lines
                g2.setColor(ThemeManager.isDarkTheme ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g2.setFont(new Font("SansSerif", Font.BOLD, 18));

                String startStr = String.valueOf(b.startTick);
                g2.drawString(startStr, x, y + height + 25);

                if (i == blocks.size() - 1) { // Draw final completion time on far right edge
                    String endStr = String.valueOf(b.endTick);
                    int ex = x + w - g2.getFontMetrics().stringWidth(endStr);
                    g2.drawString(endStr, ex, y + height + 25);
                }

                g2.setFont(new Font("SansSerif", Font.BOLD, 24)); // Restore font for next block loop
            }
        }
    }

    public JButton getNewBatchBtn() { return newBatchBtn; }
    public JButton getRestartBtn() { return restartBtn; }
    public JButton getTogglePauseBtn() { return togglePauseBtn; }
    public JButton getExitBtn() { return exitBtn; }
}