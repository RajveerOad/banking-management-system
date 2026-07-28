# Banking Management System

Banking Management System is a desktop banking application Simulator built with Java Swing for the GUI and JDBC for database connectivity. It supports separate user and admin interfaces — users can log in to check balances, deposit or withdraw funds, and view transaction history, while admins can manage user accounts. All data is persisted in a MySQL database, with credentials kept out of source control via a local configuration file.
## Requirements
- Java JDK 17+ (uses text blocks)
- MySQL Server
- MySQL Connector/J (download separately, see below — not included in this repo)

## Setup

1. **Create the database**
   Make sure MySQL is running, then create a database (default name used here is `bankdb`):
   ```sql
   CREATE DATABASE bankdb;
   ```

2. **Configure your credentials**
   Copy the example properties file and fill in your own MySQL username/password:
   ```
   cp db.properties.example db.properties
   ```
   Edit `db.properties`:
   ```
   db.url=jdbc:mysql://localhost:3306/bankdb
   db.user=your_mysql_username
   db.password=your_mysql_password
   ```
   `db.properties` is gitignored, so your real credentials will never be committed.

3. **Get the MySQL Connector/J jar**
   Download it from https://dev.mysql.com/downloads/connector/j/ (or via Maven Central) and place the `.jar` in this project folder.

4. **Compile**
   ```
   javac -cp ".:mysql-connector-j-9.5.0.jar" *.java
   ```
   (On Windows, use `;` instead of `:` in the classpath.)

5. **Run**
   ```
   java -cp ".:mysql-connector-j-9.5.0.jar" Login
   ```

   The database tables and a default admin user are created automatically on first run
   (see `Database.initializeDatabase()`).

## Notes
- Compiled `.class` files and `.jar` files are not tracked in this repo — rebuild them with the commands above.
- Never commit your real `db.properties` file.
