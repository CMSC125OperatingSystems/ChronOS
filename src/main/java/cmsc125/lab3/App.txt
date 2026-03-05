package cmsc125.lab3;

import cmsc125.lab3.controllers.AppController;
import cmsc125.lab3.views.MainFrame;

import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame();
            AppController controller = new AppController(mainFrame);

            mainFrame.setVisible(true);
            controller.startApplication();
        });
    }
}