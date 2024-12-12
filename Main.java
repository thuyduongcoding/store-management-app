import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Ensure GUI creation runs on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new LoginFrame().setVisible(true);
            }
        });
    }
}
