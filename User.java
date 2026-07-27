import java.sql.Timestamp;

public class User {
    private int id;
    private String name;
    private String password;
    private String role;
    private double balance;
    private Timestamp createdAt;

    public User(int id, String name, String password, String role, double balance, Timestamp createdAt) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.role = role;
        this.balance = balance;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public double getBalance() { return balance; }
    public Timestamp getCreatedAt() { return createdAt; }

    public void setBalance(double balance) { this.balance = balance; }
    public void setPassword(String password) { this.password = password; }

    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', role='%s', balance=%.2f}", id, name, role, balance);
    }
}
