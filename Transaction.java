import java.sql.Timestamp;

public class Transaction {
    private int id;
    private int userId;
    private String type;
    private double amount;
    private Timestamp date;
    private String details;

    public Transaction(int id, int userId, String type, double amount, Timestamp date, String details) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.details = details;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public Timestamp getDate() { return date; }
    public String getDetails() { return details; }

    @Override
    public String toString() {
        return String.format("Transaction{id=%d, type='%s', amount=%.2f, date=%s, details='%s'}",
                id, type, amount, date, details);
    }
}
