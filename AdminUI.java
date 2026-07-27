import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class AdminUI extends JFrame {

    private User admin;
    private JLabel statusLabel;

    private static final Color BG       = new Color(15, 23, 42);
    private static final Color PANEL_BG = new Color(30, 41, 59);
    private static final Color ACCENT   = new Color(37, 99, 235);
    private static final Color TEXT     = new Color(226, 232, 240);
    private static final Color MUTED    = new Color(100, 116, 139);
    private static final Color SUCCESS  = new Color(34, 197, 94);
    private static final Color DANGER   = new Color(239, 68, 68);

    public AdminUI(User admin) {
        this.admin = admin;
        setTitle("Banking System – Admin Panel");
        setSize(800, 600);
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

        JLabel title = new JLabel("ADMIN PANEL  –  " + admin.getName().toUpperCase());
        title.setFont(new Font("Monospaced", Font.BOLD, 16));
        title.setForeground(new Color(251, 191, 36));
        header.add(title, BorderLayout.WEST);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(new Font("SansSerif", Font.PLAIN, 11));
        logoutBtn.setBackground(new Color(71, 85, 105));
        logoutBtn.setForeground(TEXT);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
        logoutBtn.setFocusPainted(false);
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> { dispose(); new Login(); });
        header.add(logoutBtn, BorderLayout.EAST);

        return header;
    }

    // ── Tabs ──────────────────────────────────────────────────────────────────
    private JTabbedPane buildTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(BG);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("SansSerif", Font.PLAIN, 13));

        tabs.addTab("All Accounts", buildAccountsPanel());
        tabs.addTab("Open Account", buildOpenAccountPanel());
        tabs.addTab("Transactions", buildTransactionsPanel());

        return tabs;
    }

    // ── All Accounts Panel ────────────────────────────────────────────────────
    private JPanel buildAccountsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("All User Accounts");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(new Color(251, 191, 36));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Balance ($)", "Created At"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom buttons
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottom.setBackground(PANEL_BG);

        JButton refreshBtn = createButton("Refresh", new Color(51, 65, 85));
        JButton deleteBtn  = createButton("Delete Selected", DANGER);

        bottom.add(refreshBtn);
        bottom.add(deleteBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadAccounts(model));
        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { showStatus("Select a row first.", false); return; }
            int id = (int) model.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                "Delete account #" + id + "? This cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                if (Utils.deleteUser(id)) {
                    loadAccounts(model);
                    showStatus("Account #" + id + " deleted.", true);
                } else {
                    showStatus("Failed to delete account.", false);
                }
            }
        });

        loadAccounts(model);
        return panel;
    }

    private void loadAccounts(DefaultTableModel model) {
        model.setRowCount(0);
        for (User u : Utils.getAllUsers()) {
            model.addRow(new Object[]{
                u.getId(), u.getName(),
                String.format("%.2f", u.getBalance()),
                u.getCreatedAt().toString().substring(0, 16)
            });
        }
    }

    // ── Open Account Panel ────────────────────────────────────────────────────
    private JPanel buildOpenAccountPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(25, 40, 25, 40));

        JLabel title = new JLabel("Open New User Account");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(new Color(251, 191, 36));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(title);
        panel.add(Box.createVerticalStrut(20));

        JTextField nameField    = createTextField();
        JPasswordField passField = new JPasswordField(15);
        styleTextField(passField);
        JTextField balanceField = createTextField();

        addFormRow(panel, "Full Name:", nameField);
        addFormRow(panel, "Password:", passField);
        addFormRow(panel, "Initial Balance ($):", balanceField);

        JButton btn = createButton("CREATE ACCOUNT", new Color(5, 150, 105));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(Box.createVerticalStrut(10));
        panel.add(btn);

        btn.addActionListener(e -> {
            String name = nameField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            String balStr = balanceField.getText().trim();

            if (name.isEmpty() || pass.isEmpty() || balStr.isEmpty()) {
                showStatus("All fields are required.", false); return;
            }
            if (Utils.isNameTaken(name)) {
                showStatus("Username already exists.", false); return;
            }
            try {
                double balance = Double.parseDouble(balStr);
                if (balance < 0) { showStatus("Balance cannot be negative.", false); return; }
                if (Utils.createUser(name, pass, balance)) {
                    nameField.setText(""); passField.setText(""); balanceField.setText("");
                    showStatus("Account for '" + name + "' created successfully.", true);
                } else {
                    showStatus("Password already in use. Choose a unique password.", false);
                }
            } catch (NumberFormatException ex) {
                showStatus("Invalid balance amount.", false);
            }
        });

        return panel;
    }

    // ── Transactions Panel ────────────────────────────────────────────────────
    private JPanel buildTransactionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("View User Transactions");
        title.setFont(new Font("Monospaced", Font.BOLD, 15));
        title.setForeground(new Color(251, 191, 36));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"ID", "User ID", "Type", "Amount ($)", "Date", "Details"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        styleTable(table);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        bottom.setBackground(PANEL_BG);
        JLabel idLabel = new JLabel("Account ID:");
        idLabel.setForeground(MUTED);
        idLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JTextField idField = new JTextField(8);
        styleTextField(idField);
        idField.setMaximumSize(new Dimension(80, 30));
        JButton searchBtn = createButton("View Transactions", ACCENT);

        bottom.add(idLabel);
        bottom.add(idField);
        bottom.add(searchBtn);
        panel.add(bottom, BorderLayout.SOUTH);

        searchBtn.addActionListener(e -> {
            try {
                int userId = Integer.parseInt(idField.getText().trim());
                User u = Utils.getUserById(userId);
                if (u == null) { showStatus("Account #" + userId + " not found.", false); return; }
                model.setRowCount(0);
                List<Transaction> txs = Utils.getTransactions(userId);
                if (txs.isEmpty()) { showStatus("No transactions for Account #" + userId + ".", false); return; }
                for (Transaction tx : txs) {
                    model.addRow(new Object[]{
                        tx.getId(), tx.getUserId(),
                        tx.getType().toUpperCase(),
                        String.format("%.2f", tx.getAmount()),
                        tx.getDate().toString().substring(0, 16),
                        tx.getDetails()
                    });
                }
                showStatus("Showing " + txs.size() + " transactions for " + u.getName(), true);
            } catch (NumberFormatException ex) {
                showStatus("Enter a valid numeric Account ID.", false);
            }
        });

        return panel;
    }

    // ── Status Bar ────────────────────────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bar.setBackground(BG);
        statusLabel = new JLabel("Admin session active.");
        statusLabel.setFont(new Font("Monospaced", Font.PLAIN, 11));
        statusLabel.setForeground(MUTED);
        bar.add(statusLabel);
        return bar;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void showStatus(String msg, boolean ok) {
        statusLabel.setText(msg);
        statusLabel.setForeground(ok ? SUCCESS : DANGER);
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
        btn.setBorder(BorderFactory.createEmptyBorder(7, 16, 7, 16));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
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

    private void styleTable(JTable table) {
        table.setBackground(new Color(15, 23, 42));
        table.setForeground(TEXT);
        table.setGridColor(new Color(51, 65, 85));
        table.setFont(new Font("Monospaced", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setBackground(new Color(30, 41, 59));
        table.getTableHeader().setForeground(new Color(251, 191, 36));
        table.getTableHeader().setFont(new Font("Monospaced", Font.BOLD, 12));
        table.setSelectionBackground(new Color(37, 99, 235));
    }
}
