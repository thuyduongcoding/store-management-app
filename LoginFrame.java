import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;

    public LoginFrame() {
        setTitle("Retail Store Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centers the window

        // Create panels
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Username Label
        JLabel usernameLabel = new JLabel("Username:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10,10,10,10);
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(usernameLabel, gbc);

        // Username Field
        usernameField = new JTextField(20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(usernameField, gbc);

        // Password Label
        JLabel passwordLabel = new JLabel("Password:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        panel.add(passwordLabel, gbc);

        // Password Field
        passwordField = new JPasswordField(20);
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(passwordField, gbc);

        // Login Button
        loginButton = new JButton("Login");
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        panel.add(loginButton, gbc);

        // Add Action Listener to Login Button
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleLogin();
            }
        });

        add(panel);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        char[] passwordChars = passwordField.getPassword();
        String password = new String(passwordChars);

        // Authenticate user
        User loggedInUser = AuthService.login(username, password);
        if (loggedInUser != null) {
            JOptionPane.showMessageDialog(this, "Login successful! Welcome, " + loggedInUser.getUsername() + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
            this.dispose(); // Close login window
            openRoleBasedFrame(loggedInUser);
        } else {
            JOptionPane.showMessageDialog(this, "Login failed. Check your username and password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openRoleBasedFrame(User user) {
        String role = user.getRole().toLowerCase();
        switch(role) {
            case "customer":
                SwingUtilities.invokeLater(() -> {
                    Customer customer = DataStore.getCustomerByUserId(user.getUserId());
                    if (customer == null) {
                        JOptionPane.showMessageDialog(this, "Customer profile not found. Please contact support.", "Error", JOptionPane.ERROR_MESSAGE);
                        System.exit(0);
                    } else {
                        new CustomerFrame(user, customer).setVisible(true);
                    }
                });
                break;
            case "cashier":
                SwingUtilities.invokeLater(() -> {
                    new CashierFrame(user).setVisible(true);
                });
                break;
            case "manager":
                SwingUtilities.invokeLater(() -> {
                    new ManagerFrame(user).setVisible(true);
                });
                break;
            default:
                JOptionPane.showMessageDialog(this, "Unknown role. Access denied.", "Error", JOptionPane.ERROR_MESSAGE);
                break;
        }
    }
}
