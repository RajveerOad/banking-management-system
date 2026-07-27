import java.awt.*;
import javax.swing.*;

public class Login extends JFrame {

    private JTextField nameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;

    public Login() {
        setTitle("Banking System – Login");
        setSize(420, 320);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // ── Main Panel ─────────────────────────────────────────────────────
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(15, 23, 42));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        add(panel);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // ── Title ──────────────────────────────────────────────────────────
        JLabel title = new JLabel("BANK SYSTEM", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(new Color(99, 179, 237));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(title, gbc);

        JLabel subtitle = new JLabel("Secure Banking Portal", SwingConstants.CENTER);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        subtitle.setForeground(new Color(100, 116, 139));
        gbc.gridy = 1;
        panel.add(subtitle, gbc);

        // ── Spacer ─────────────────────────────────────────────────────────
        gbc.gridy = 2; gbc.gridwidth = 2;
        panel.add(Box.createVerticalStrut(10), gbc);

        // ── Name Field ─────────────────────────────────────────────────────
        gbc.gridwidth = 1;
        JLabel nameLabel = new JLabel("Username");
        nameLabel.setForeground(new Color(148, 163, 184));
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 3;
        panel.add(nameLabel, gbc);

        nameField = new JTextField(15);
        styleTextField(nameField);
        gbc.gridx = 1; gbc.gridy = 3;
        panel.add(nameField, gbc);

        // ── Password Field ─────────────────────────────────────────────────
        JLabel passLabel = new JLabel("Password");
        passLabel.setForeground(new Color(148, 163, 184));
        passLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 4;
        panel.add(passLabel, gbc);

        passwordField = new JPasswordField(15);
        styleTextField(passwordField);
        gbc.gridx = 1; gbc.gridy = 4;
        panel.add(passwordField, gbc);

        // ── Login Button ───────────────────────────────────────────────────
        JButton loginBtn = new JButton("LOGIN");
        loginBtn.setBackground(new Color(37, 99, 235));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Monospaced", Font.BOLD, 13));
        loginBtn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        loginBtn.setFocusPainted(false);
        loginBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
        panel.add(loginBtn, gbc);

        // ── Message Label ──────────────────────────────────────────────────
        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.ITALIC, 11));
        messageLabel.setForeground(new Color(239, 68, 68));
        gbc.gridy = 6;
        panel.add(messageLabel, gbc);

        // ── Action ─────────────────────────────────────────────────────────
        loginBtn.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());

        setVisible(true);
    }

    private void handleLogin() {
        String name = nameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (name.isEmpty() || password.isEmpty()) {
            showMessage("Please enter both username and password.", false);
            return;
        }

        User user = Utils.login(name, password);

        if (user == null) {
            showMessage("Invalid username or password.", false);
            passwordField.setText("");
        } else {
            showMessage("Login successful! Redirecting...", true);
            dispose();
            if ("admin".equals(user.getRole())) {
                new AdminUI(user);
            } else {
                new UserUI(user);
            }
        }
    }

    private void showMessage(String msg, boolean success) {
        messageLabel.setText(msg);
        messageLabel.setForeground(success ? new Color(34, 197, 94) : new Color(239, 68, 68));
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(30, 41, 59));
        field.setForeground(new Color(226, 232, 240));
        field.setCaretColor(new Color(99, 179, 237));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setFont(new Font("Monospaced", Font.PLAIN, 13));
    }

    public static void main(String[] args) {
        Database.initializeDatabase();
        SwingUtilities.invokeLater(Login::new);
    }
}
