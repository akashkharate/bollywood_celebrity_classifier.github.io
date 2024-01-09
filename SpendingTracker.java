package spending;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class SpendingTracker {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/spending_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "super30@M";

    public static void main(String[] args) {
        try (Connection connection = DbConnect.getConnection()) {
            createTablesIfNotExist(connection);

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n**********************select option to perform operation******************");
                System.out.println("1. Add Expense");
                System.out.println("2. View Expenses");
                System.out.println("3. Update Expense");
                System.out.println("4. Delete Expense");
                System.out.println("5. Add New Category");
                System.out.println("6. Exit");
                System.out.print("Choose an option: ");

                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume the newline character

                switch (choice) {
                    case 1:
                        Category.addExpense(scanner, connection);
                        break;
                    case 2:
                        Category.viewExpenses(connection);
                        break;
                    case 3:
                        Category.updateExpense(scanner, connection);
                        break;
                    case 4:
                        Category.deleteExpense(scanner, connection);
                        break;
                    case 5:
                        Category.addNewCategory(scanner, connection);
                        break;
                    case 6:
                        System.out.println("Exiting the Spending Tracker. Goodbye!");
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTablesIfNotExist(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            String createExpensesTableSQL = "CREATE TABLE IF NOT EXISTS expenses (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "description VARCHAR(255), " +
                    "amount DOUBLE, " +
                    "category_id INT, " +
                    "date DATE)";

            String createCategoriesTableSQL = "CREATE TABLE IF NOT EXISTS categories (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "category_name VARCHAR(50))";

            statement.executeUpdate(createExpensesTableSQL);
            statement.executeUpdate(createCategoriesTableSQL);
        }
    }
}
