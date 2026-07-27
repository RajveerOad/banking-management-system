import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    // ─── Authentication ───────────────────────────────────────────────────────

    public static User login(String name, String password) {
        String sql = "SELECT * FROM users WHERE name = ? AND password = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getDouble("balance"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return null;
    }

    // ─── Balance ──────────────────────────────────────────────────────────────

    public static double getBalance(int userId) {
        String sql = "SELECT balance FROM users WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("balance");
        } catch (SQLException e) {
            System.err.println("Get balance error: " + e.getMessage());
        }
        return -1;
    }

    // ─── Deposit ──────────────────────────────────────────────────────────────

    public static boolean deposit(int userId, double amount) {
        if (amount <= 0) return false;
        String updateBalance = "UPDATE users SET balance = balance + ? WHERE id = ?";
        String insertTx = "INSERT INTO transactions (user_id, type, amount, details) VALUES (?, 'deposit', ?, 'Deposit')";
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(updateBalance);
                 PreparedStatement ps2 = con.prepareStatement(insertTx)) {
                ps1.setDouble(1, amount);
                ps1.setInt(2, userId);
                ps1.executeUpdate();

                ps2.setInt(1, userId);
                ps2.setDouble(2, amount);
                ps2.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Deposit error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
        return false;
    }

    // ─── Withdraw ─────────────────────────────────────────────────────────────

    public static boolean withdraw(int userId, double amount) {
        if (amount <= 0) return false;
        double balance = getBalance(userId);
        if (balance < amount) return false;

        String updateBalance = "UPDATE users SET balance = balance - ? WHERE id = ?";
        String insertTx = "INSERT INTO transactions (user_id, type, amount, details) VALUES (?, 'withdraw', ?, 'Withdrawal')";
        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(updateBalance);
                 PreparedStatement ps2 = con.prepareStatement(insertTx)) {
                ps1.setDouble(1, amount);
                ps1.setInt(2, userId);
                ps1.executeUpdate();

                ps2.setInt(1, userId);
                ps2.setDouble(2, amount);
                ps2.executeUpdate();

                con.commit();
                return true;
            } catch (SQLException e) {
                con.rollback();
                System.err.println("Withdraw error: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Connection error: " + e.getMessage());
        }
        return false;
    }

    // ─── Transfer ─────────────────────────────────────────────────────────────

    public static String transfer(int senderId, int recipientId, double amount) {
        if (amount <= 0) return "Amount must be greater than 0.";
        if (senderId == recipientId) return "Cannot transfer to yourself.";

        double balance = getBalance(senderId);
        if (balance < amount) return "Insufficient balance.";

        User recipient = getUserById(recipientId);
        if (recipient == null) return "Recipient account not found.";

        String deduct = "UPDATE users SET balance = balance - ? WHERE id = ?";
        String add    = "UPDATE users SET balance = balance + ? WHERE id = ?";
        String txSender    = "INSERT INTO transactions (user_id, type, amount, details) VALUES (?, 'transfer', ?, ?)";
        String txRecipient = "INSERT INTO transactions (user_id, type, amount, details) VALUES (?, 'transfer', ?, ?)";

        try (Connection con = Database.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement ps1 = con.prepareStatement(deduct);
                 PreparedStatement ps2 = con.prepareStatement(add);
                 PreparedStatement ps3 = con.prepareStatement(txSender);
                 PreparedStatement ps4 = con.prepareStatement(txRecipient)) {

                ps1.setDouble(1, amount); ps1.setInt(2, senderId); ps1.executeUpdate();
                ps2.setDouble(1, amount); ps2.setInt(2, recipientId); ps2.executeUpdate();

                ps3.setInt(1, senderId); ps3.setDouble(2, amount);
                ps3.setString(3, "Transferred to Account #" + recipientId + " (" + recipient.getName() + ")");
                ps3.executeUpdate();

                ps4.setInt(1, recipientId); ps4.setDouble(2, amount);
                ps4.setString(3, "Received from Account #" + senderId);
                ps4.executeUpdate();

                con.commit();
                return "SUCCESS";
            } catch (SQLException e) {
                con.rollback();
                return "Transfer failed: " + e.getMessage();
            }
        } catch (SQLException e) {
            return "Connection error: " + e.getMessage();
        }
    }

    // ─── Transaction History ──────────────────────────────────────────────────

    public static List<Transaction> getTransactions(int userId) {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Transaction(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getTimestamp("date"),
                    rs.getString("details")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Get transactions error: " + e.getMessage());
        }
        return list;
    }

    // ─── Password ─────────────────────────────────────────────────────────────

    public static boolean isPasswordUnique(String password) {
        String sql = "SELECT COUNT(*) FROM users WHERE password = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) == 0;
        } catch (SQLException e) {
            System.err.println("Password check error: " + e.getMessage());
        }
        return false;
    }

    public static boolean updatePassword(int userId, String newPassword) {
        if (!isPasswordUnique(newPassword)) return false;
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Update password error: " + e.getMessage());
        }
        return false;
    }

    // ─── Admin Helpers ────────────────────────────────────────────────────────

    public static List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users WHERE role = 'user' ORDER BY id";
        try (Connection con = Database.getConnection();
             Statement stmt = con.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getDouble("balance"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Get all users error: " + e.getMessage());
        }
        return list;
    }

    public static boolean createUser(String name, String password, double initialBalance) {
        if (!isPasswordUnique(password)) return false;
        String sql = "INSERT INTO users (name, password, role, balance) VALUES (?, ?, 'user', ?)";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setString(2, password);
            ps.setDouble(3, initialBalance);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Create user error: " + e.getMessage());
        }
        return false;
    }

    public static boolean deleteUser(int userId) {
        String sql = "DELETE FROM users WHERE id = ? AND role = 'user'";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Delete user error: " + e.getMessage());
        }
        return false;
    }

    public static User getUserById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getDouble("balance"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            System.err.println("Get user error: " + e.getMessage());
        }
        return null;
    }

    public static boolean isNameTaken(String name) {
        String sql = "SELECT COUNT(*) FROM users WHERE name = ?";
        try (Connection con = Database.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("Name check error: " + e.getMessage());
        }
        return false;
    }
}
