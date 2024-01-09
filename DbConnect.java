package spending;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnect {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/spending_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "super30@M";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }
}
