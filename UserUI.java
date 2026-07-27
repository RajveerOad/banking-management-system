import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class UserUI extends JFrame {

    private User user;
    private JLabel balanceLabel;
    private JLabel statusLabel;

    // ── Colors ────────────────────────────────────────────────────────────────
    private static final Color BG       = new Color(15, 23, 42);
    private static final Color PANEL_BG = new Color(30, 41, 59);
    private static final Color ACCENT   = new Color(37, 99, 235);
    private static final Color TEXT     = new Color(226, 232, 240);
    private static final Color MUTED    = new Color(100, 116, 139);
    private static final Color SUCCESS  = new Color(34, 197, 94);
    private static final Color DANGER   = new Color(239, 68, 68);

    public UserUI(User user) {
        this.user = user;
        setTitle("Banking System – " + user.getName());
        setSize(700, 580);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBackground(BG);
        root.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(root);

        root.add(buildHeader(), BorderLayout.NORTH);
        root.add(buildTabs(), BorderLayout.CENTER);
        root.add(buildStatusBar(), BorderLayout.SOUTH);

        setVisible(true);
    }

    // ── Header ────────────────────────────────────────────────────────────────
    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PANEL_BG);
        header.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel welcome = new JLabel("Welcome, " + user.getName());
        welcome.setFont(new Font("Monospaced", Font.BOLD, 16));
        welcome.setForeground(new Color(99, 179, 237));
        header.add(welcome, BorderLayout.WEST);

        refreshBalance();
        balanceLabel = new JLabel("Balance: $" + String.format("%.2f", user.getBalance()));
        balanceLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        balanceLabel.setForeground(SUCCESS);
        header.add(balanceLabel, BorderLayout.EAST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logoutBtn.setBackground(new Color(71, 85, 105));
        logoutBtn.setForeground(TEXT);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> { dispose(); new Login(); });

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        right.setBackground(PANEL_BG);
        right.add(balanceLabel);
        right.add(logoutBtn);
        header.add(right, BorderLayout.EAST);

        return header;
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabs.addTab("Deposit", buildDepositPanel());
        tabs.addTab("Withdraw", buildWithdrawPanel());
        tabs.addTab("Transfer", buildTransferPanel());
        tabs.addTab("History", buildHistoryPanel());
        tabs.addTab("Password", buildPasswordPanel());

        return tabs;
    }

    // ── Deposit Panel ─────────────────────────────────────────────────────────
    private JPanel buildDepositPanel() {
        JPanel panel = createFormPanel();
        addTitle(panel, "Deposit Money");

        JTextField amountField = createTextField();
        addFormRow(panel, "Amount ($):", amountField);

        JButton btn = createButton("DEPOSIT", ACCENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btn);

        btn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (Utils.deposit(user.getId(), amount)) {
                    refreshBalance();
                    amountField.setText("");
                    showStatus("Deposited $" + String.format("%.2f", amount) + " successfully.", true);
                } else {
                    showStatus("Deposit failed. Amount must be > 0.", false);
                }
            } catch (NumberFormatException ex) {
                showStatus("Invalid amount. Enter a number.", false);
            }
        });

        return panel;
    }

    // ── Withdraw Panel ────────────────────────────────────────────────────────
    private JPanel buildWithdrawPanel() {
        JPanel panel = createFormPanel();
        addTitle(panel, "Withdraw Money");

        JTextField amountField = createTextField();
        addFormRow(panel, "Amount ($):", amountField);

        JButton btn = createButton("WITHDRAW", new Color(185, 28, 28));
        panel.add(Box.createVerticalStrut(10));
        panel.add(btn);

        btn.addActionListener(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText().trim());
                if (Utils.withdraw(user.getId(), amount)) {
                    refreshBalance();
                    amountField.setText("");
                    showStatus("Withdrew $" + String.format("%.2f", amount) + " successfully.", true);
                } else {
                    showStatus("Insufficient balance or invalid amount.", false);
                }
            } catch (NumberFormatException ex) {
                showStatus("Invalid amount. Enter a number.", false);
            }
        });

        return panel;
    }

    // ── Transfer Panel ────────────────────────────────────────────────────────
    private JPanel buildTransferPanel() {
        JPanel panel = createFormPanel();
        addTitle(panel, "Transfer Money");

        JTextField recipientField = createTextField();
        JTextField amountField = createTextField();
        addFormRow(panel, "Recipient Account ID:", recipientField);
        addFormRow(panel, "Amount ($):", amountField);

        JButton btn = createButton("TRANSFER", new Color(109, 40, 217));
        panel.add(Box.createVerticalStrut(10));
        panel.add(btn);

        btn.addActionListener(e -> {
            try {
                int recipientId = Integer.parseInt(recipientField.getText().trim());
                double amount = Double.parseDouble(amountField.getText().trim());
                String result = Utils.transfer(user.getId(), recipientId, amount);
                if ("SUCCESS".equals(result)) {
                    refreshBalance();
                    recipientField.setText("");
                    amountField.setText("");
                    showStatus("Transfer of $" + String.format("%.2f", amount) + " completed.", true);
                } else {
                    showStatus(result, false);
                }
            } catch (NumberFormatException ex) {
                showStatus("Invalid input. Check Account ID and amount.", false);
            }
        });

        return panel;
    }

    // ── History Panel ─────────────────────────────────────────────────────────
    private JPanel buildHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Transaction History");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(new Color(99, 179, 237));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Type", "Amount ($)", "Date", "Details"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JButton refresh = createButton("Refresh", new Color(51, 65, 85));
        refresh.setPreferredSize(new Dimension(100, 30));
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(PANEL_BG);
        bottom.add(refresh);
        panel.add(bottom, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadHistory(model));
        loadHistory(model);

        return panel;
    }

    private void loadHistory(DefaultTableModel model) {
        model.setRowCount(0);
        List<Transaction> txs = Utils.getTransactions(user.getId());
        for (Transaction tx : txs) {
            model.addRow(new Object[]{
                tx.getId(),
                tx.getType().toUpperCase(),
                String.format("%.2f", tx.getAmount()),
                tx.getDate().toString().substring(0, 16),
                tx.getDetails()
            });
        }
    }

    // ── Password Panel ────────────────────────────────────────────────────────
    private JPanel buildPasswordPanel() {
        JPanel panel = createFormPanel();
        addTitle(panel, "Update Password");

        JPasswordField newPassField = new JPasswordField(15);
        styleTextField(newPassField);
        JPasswordField confirmField = new JPasswordField(15);
        styleTextField(confirmField);
        addFormRow(panel, "New Password:", newPassField);
        addFormRow(panel, "Confirm Password:", confirmField);

        JButton btn = createButton("UPDATE PASSWORD", new Color(5, 150, 105));
        panel.add(Box.createVerticalStrut(10));
        panel.add(btn);

        btn.addActionListener(e -> {
            String newPass = new String(newPassField.getPassword());
            String confirm = new String(confirmField.getPassword());
            if (newPass.isEmpty()) { showStatus("Password cannot be empty.", false); return; }
            if (!newPass.equals(confirm)) { showStatus("Passwords do not match.", false); return; }
            if (Utils.updatePassword(user.getId(), newPass)) {
                newPassField.setText(""); confirmField.setText("");
                showStatus("Password updated successfully.", true);
            } else {
                showStatus("Password already in use. Choose a unique password.", false);
            }
        });

        return panel;
    }

    // ── Status Bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(new Color(15, 23, 42));
        statusLabel = new JLabel("Ready.");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusLabel.setForeground(MUTED);
        bar.add(statusLabel);
        return bar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void refreshBalance() {
        double b = Utils.getBalance(user.getId());
        user.setBalance(b);
        if (balanceLabel != null) balanceLabel.setText("Balance: $" + String.format("%.2f", b));
    }

    private void showStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.setForeground(ok ? SUCCESS : DANGER);
    }

    private JPanel createFormPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(PANEL_BG);
        p.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));
        return p;
    }

    private void addTitle(JPanel p, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Monospaced", Font.BOLD, 15));
        lbl.setForeground(new Color(99, 179, 237));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lbl);
        p.add(Box.createVerticalStrut(20));
    }

    private void addFormRow(JPanel panel, String labelText, JComponent field) {
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lbl.setForeground(MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(300, 32));
        panel.add(lbl);
        panel.add(Box.createVerticalStrut(4));
        panel.add(field);
        panel.add(Box.createVerticalStrut(12));
    }

    private JTextField createTextField() {
        JTextField f = new JTextField(15);
        styleTextField(f);
        return f;
    }

    private void styleTextField(JTextField field) {
        field.setBackground(new Color(15, 23, 42));
        field.setForeground(TEXT);
        field.setCaretColor(new Color(99, 179, 237));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        field.setFont(new Font("Monospaced", Font.PLAIN, 13));
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Monospaced", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        return btn;
    }

    private void styleTable(JTable table) {
        table.setBackground(new Color(15, 23, 42));
        table.setForeground(TEXT);
        table.setGridColor(new Color(51, 65, 85));
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setBackground(new Color(30, 41, 59));
        table.getTableHeader().setForeground(new Color(99, 179, 237));
        table.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
        table.setSelectionBackground(new Color(37, 99, 235));
    }
}
