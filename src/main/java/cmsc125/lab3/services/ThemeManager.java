package cmsc125.lab3.services;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class ThemeManager {
    public static boolean isDarkTheme = true;

    public static void applyTheme(Component comp, boolean isDark) {
        isDarkTheme = isDark;

        Color bgPanel = isDark ? new Color(43, 43, 43) : new Color(240, 240, 240);
        Color bgInput = isDark ? new Color(60, 60, 60) : Color.WHITE;
        Color fgText = isDark ? Color.WHITE : Color.BLACK;
        Color btnBg = isDark ? new Color(75, 75, 75) : new Color(220, 220, 220);

        if (comp instanceof JFrame) {
            comp.setBackground(bgPanel);
        }

        if (comp instanceof JPanel || comp instanceof JScrollPane || comp instanceof JViewport) {
            if (comp.isOpaque()) {
                comp.setBackground(bgPanel);
            }
            comp.setForeground(fgText);
        }

        if (comp instanceof JLabel || comp instanceof JCheckBox) {
            comp.setForeground(fgText);
        }

        if (comp instanceof JButton) {
            JButton btn = (JButton) comp;
            // Prevent overriding custom icon buttons or our colored toggle buttons
            if (!btn.isContentAreaFilled()) {
                btn.setForeground(fgText);
            } else if (!btn.getText().equals("Enabled") && !btn.getText().equals("Disabled")) {
                btn.setBackground(btnBg);
                btn.setForeground(fgText);
            }
        }

        if (comp instanceof JTable) {
            JTable table = (JTable) comp;
            table.setBackground(bgInput);
            table.setForeground(fgText);
            table.setGridColor(isDark ? Color.GRAY : Color.LIGHT_GRAY);

            JTableHeader header = table.getTableHeader();
            if (header != null) {
                header.setBackground(btnBg);
                header.setForeground(fgText);
            }
        }

        if (comp instanceof JComboBox || comp instanceof JSpinner || comp instanceof JTextField) {
            comp.setBackground(bgInput);
            comp.setForeground(fgText);
        }

        // Recursively apply to children
        if (comp instanceof Container) {
            for (Component child : ((Container) comp).getComponents()) {
                applyTheme(child, isDark);
            }
        }
    }
}