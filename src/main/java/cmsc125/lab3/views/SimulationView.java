package cmsc125.lab3.views;

import cmsc125.lab3.models.ProcessModel;
import cmsc125.lab3.services.ThemeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationView extends JPanel {
    private final JLabel infoLabel, timeLabel;
    private final DefaultTableModel resultTableModel;
    private final GanttChartPanel ganttChartPanel;
    private final JScrollPane ganttScrollPane;
    private final JButton newBatchBtn, restartBtn, togglePauseBtn, exitBtn;
    private final JComboBox<String> speedCombo;
    private final TableColumn priorityColumnRes;
    private final JTable resultTable;

    private final Map<String, Color> processColors = new HashMap<>();
    private final Color[] palette = {
            Color.RED, Color.BLUE, new Color(0, 150, 0), Color.ORANGE, Color.MAGENTA, new Color(34, 139, 34),
            new Color(255, 105, 180), new Color(150, 150, 0), new Color(128, 0, 128), new Color(0, 128, 128)
    };

    public SimulationView() {
        setLayout(new BorderLayout(15, 15));

        infoLabel = new JLabel("Method | Algorithm");
        infoLabel.setFont(new Font("SansSerif", Font.BOLD, 28));
        infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(infoLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        String[] cols = {"Process ID", "Burst Time", "Arrival Time", "Priority", "Waiting Time", "Turnaround Time", "AWT", "ATT"};

        resultTableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        resultTable = new JTable(resultTableModel);
        resultTable.setFont(new Font("SansSerif", Font.PLAIN, 20));
        resultTable.setRowHeight(40);
        resultTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 22));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < resultTable.getColumnCount(); i++) {
            resultTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        centerPanel.add(new JScrollPane(resultTable), BorderLayout.CENTER);
        centerPanel.add(Box.createHorizontalStrut(50), BorderLayout.WEST);
        centerPanel.add(Box.createHorizontalStrut(50), BorderLayout.EAST);

        add(centerPanel, BorderLayout.CENTER);

        JLabel avgWtLabel = new JLabel();
        JLabel avgTatLabel = new JLabel();

        JPanel bottomPanel = new JPanel(new BorderLayout());

        JPanel ganttContainer = new JPanel(new BorderLayout());
        ganttContainer.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        timeLabel = new JLabel("Time: 00:00");
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        ganttContainer.add(timeLabel, BorderLayout.NORTH);

        ganttChartPanel = new GanttChartPanel();
        ganttScrollPane = new JScrollPane(ganttChartPanel);
        ganttScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        ganttScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScrollPane.setPreferredSize(new Dimension(800, 140));
        ganttScrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        ganttContainer.add(ganttScrollPane, BorderLayout.CENTER);
        bottomPanel.add(ganttContainer, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        newBatchBtn = new JButton("Process New Batch");
        restartBtn = new JButton("Restart");
        togglePauseBtn = new JButton("Pause");
        exitBtn = new JButton("Exit");

        Font btnFont = new Font("SansSerif", Font.BOLD, 18);
        newBatchBtn.setFont(btnFont); restartBtn.setFont(btnFont);
        togglePauseBtn.setFont(btnFont); exitBtn.setFont(btnFont);

        JLabel speedLabel = new JLabel("Speed:");
        speedLabel.setFont(btnFont);
        speedCombo = new JComboBox<>(new String[]{"1.0x", "1.5x", "2.0x", "3.0x", "5.0x"});
        speedCombo.setFont(btnFont);

        controlsPanel.add(newBatchBtn);
        controlsPanel.add(restartBtn);
        controlsPanel.add(togglePauseBtn);
        controlsPanel.add(speedLabel);
        controlsPanel.add(speedCombo);
        controlsPanel.add(exitBtn);

        bottomPanel.add(controlsPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        priorityColumnRes = resultTable.getColumnModel().getColumn(3);
    }

    public void setupSimulation(String method, String algorithm, List<ProcessModel> processes) {
        boolean isPriority = algorithm != null && algorithm.toLowerCase().contains("priority");

        if (!isPriority) {
            try { resultTable.removeColumn(priorityColumnRes); } catch (Exception ignored) {}
        } else {
            try {
                resultTable.removeColumn(priorityColumnRes);
                resultTable.addColumn(priorityColumnRes);
                resultTable.moveColumn(resultTable.getColumnCount() - 1, 3);
            } catch (Exception ignored) {}
        }

        infoLabel.setText(method + "   |   " + algorithm);
        resultTableModel.setRowCount(0);
        processColors.clear();
        ganttChartPanel.reset();

        int cIdx = 0;
        for (ProcessModel p : processes) {
            resultTableModel.addRow(new Object[]{
                    p.getProcessId(), p.getBurstTime(), p.getArrivalTime(), p.getPriority(),
                    "", "", "0.00", "0.00"
            });
            processColors.put(p.getProcessId(), palette[cIdx % palette.length]);
            cIdx++;
        }

        timeLabel.setText("Time: 00:00");
    }

    public void addGanttBlock(String processId, int tick) {
        Color color = (processId == null || processId.equals("IDLE")) ?
                (ThemeManager.isDarkTheme ? Color.DARK_GRAY : Color.LIGHT_GRAY) :
                processColors.getOrDefault(processId, Color.GRAY);

        ganttChartPanel.addTick(processId == null ? "IDLE" : processId, tick, color);
        timeLabel.setText(String.format("Time: %s", formatToMMSS(tick + 1)));
        SwingUtilities.invokeLater(() -> {
            JScrollBar horizontal = ganttScrollPane.getHorizontalScrollBar();
            horizontal.setValue(horizontal.getMaximum());
        });
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

    private String formatToMMSS(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public void updateAverages(double avgWt, double avgTat) {
        String wtStr = String.format("%.2f", avgWt);
        String tatStr = String.format("%.2f", avgTat);


        for (int i = 0; i < resultTableModel.getRowCount(); i++) {
            resultTableModel.setValueAt(wtStr, i, 6);
            resultTableModel.setValueAt(tatStr, i, 7);
        }
    }

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
        final int PIXELS_PER_TICK = 40, BAR_HEIGHT = 50;

        public GanttChartPanel() {
            setPreferredSize(new Dimension(800, 100));
        }

        public void addTick(String processId, int tick, Color color) {
            if (blocks.isEmpty()) {
                blocks.add(new GanttBlock(processId, tick, tick + 1, color));
            } else {
                GanttBlock last = blocks.get(blocks.size() - 1);
                if (last.processId.equals(processId) && last.endTick == tick) {
                    last.endTick = tick + 1;
                } else blocks.add(new GanttBlock(processId, tick, tick + 1, color));
            }
            totalTime = tick + 1;

            int newWidth = Math.max(800, (totalTime * PIXELS_PER_TICK) + 50);
            setPreferredSize(new Dimension(newWidth, 100));
            revalidate();
            repaint();
        }

        public void reset() {
            blocks.clear();
            totalTime = 0;
            setPreferredSize(new Dimension(800, 100));
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (blocks.isEmpty()) return;

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int y = 20;
            g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            FontMetrics fm = g2.getFontMetrics();

            for (GanttBlock b : blocks) {
                int x = b.startTick * PIXELS_PER_TICK;
                int w = (b.endTick - b.startTick) * PIXELS_PER_TICK;

                g2.setColor(b.color);
                g2.fillRect(x, y, w, BAR_HEIGHT);

                g2.setColor(ThemeManager.isDarkTheme ? Color.WHITE : Color.BLACK);
                g2.setStroke(new BasicStroke(2));
                g2.drawRect(x, y, w, BAR_HEIGHT);

                String text = b.processId.equals("IDLE") ? "-" : b.processId;

                double brightness = (b.color.getRed() * 0.299) + (b.color.getGreen() * 0.587) + (b.color.getBlue() * 0.114);
                Color textColor = (brightness < 128) ? Color.WHITE : Color.BLACK;

                if (b.processId.equals("IDLE")) {
                    textColor = ThemeManager.isDarkTheme ? Color.WHITE : Color.BLACK;
                }

                g2.setColor(textColor);
                if (w > fm.stringWidth(text) + 5) {
                    int tx = x + (w - fm.stringWidth(text)) / 2;
                    int ty = y + ((BAR_HEIGHT - fm.getHeight()) / 2) + fm.getAscent();
                    g2.drawString(text, tx, ty);
                }

                g2.setColor(ThemeManager.isDarkTheme ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString(formatToMMSS(b.startTick), x, y + BAR_HEIGHT + 15);
                g2.setFont(new Font("SansSerif", Font.BOLD, 20));
            }

            if (totalTime > 0) {
                g2.setColor(ThemeManager.isDarkTheme ? Color.LIGHT_GRAY : Color.DARK_GRAY);
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
                g2.drawString(formatToMMSS(totalTime), totalTime * PIXELS_PER_TICK, y + BAR_HEIGHT + 15);
            }
        }
    }

    public JButton getNewBatchBtn() { return newBatchBtn; }
    public JButton getRestartBtn() { return restartBtn; }
    public JButton getTogglePauseBtn() { return togglePauseBtn; }
    public JButton getExitBtn() { return exitBtn; }
    public JComboBox<String> getSpeedCombo() { return speedCombo; }
}