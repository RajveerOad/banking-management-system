import java.sql.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Database {
    private static String URL;
    private static String USER;
    private static String PASSWORD;

    static {
        Properties props = new Properties();
        try (FileInputStream in = new FileInputStream("db.properties")) {
            props.load(in);
            URL = props.getProperty("db.url");
            USER = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            System.err.println("Could not load db.properties. Make sure it exists in the project root " +
                    "(copy db.properties.example to db.properties and fill in your own values).");
            throw new RuntimeException(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id INT PRIMARY KEY AUTO_INCREMENT,
                name VARCHAR(100) NOT NULL,
                password VARCHAR(100) NOT NULL UNIQUE,
                role ENUM('user','admin') NOT NULL DEFAULT 'user',
                balance DOUBLE NOT NULL DEFAULT 0.0,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        """;

        String createTransactions = """
            CREATE TABLE IF NOT EXISTS transactions (
                id INT PRIMARY KEY AUTO_INCREMENT,
                user_id INT NOT NULL,
                type ENUM('deposit','withdraw','transfer') NOT NULL,
                amount DOUBLE NOT NULL,
                date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                details VARCHAR(255),
                FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
            )
        """;

        String insertAdmin = """
            INSERT IGNORE INTO users (name, password, role, balance)
            VALUES ('admin', 'admin123', 'admin', 0.0)
        """;

        try (Connection con = getConnection();
             Statement stmt = con.createStatement()) {
            stmt.execute(createUsers);
            stmt.execute(createTransactions);
            stmt.execute(insertAdmin);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }
}
