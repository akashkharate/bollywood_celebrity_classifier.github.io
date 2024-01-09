import java.sql.*;
import java.util.Scanner;

public class SpendingTracker {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/spending_tracker";
    private static final String USER = "root";
    private static final String PASSWORD = "super30@M";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(JDBC_URL, USER, PASSWORD)) {
            createTablesIfNotExist(connection);

            Scanner scanner = new Scanner(System.in);

            while (true) {
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
                        addExpense(scanner, connection);
                        break;
                    case 2:
                        viewExpenses(connection);
                        break;
                    case 3:
                        updateExpense(scanner, connection);
                        break;
                    case 4:
                        deleteExpense(scanner, connection);
                        break;
                    case 5:
                        addNewCategory(scanner, connection);
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

    private static void addExpense(Scanner scanner, Connection connection) throws SQLException {
        System.out.print("Enter expense description: ");
        String description = scanner.nextLine();

        System.out.print("Enter expense amount: ");
        double amount = scanner.nextDouble();

        displayCategories(connection);

        int categoryId = selectCategory(scanner, connection);

        System.out.print("Enter expense date (YYYY-MM-DD): ");
        String date = scanner.next();

        String insertExpenseSQL = "INSERT INTO expenses (description, amount, category_id, date) VALUES (?, ?, ?, ?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertExpenseSQL)) {
            preparedStatement.setString(1, description);
            preparedStatement.setDouble(2, amount);
            preparedStatement.setInt(3, categoryId);
            preparedStatement.setString(4, date);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("Expense added successfully!");
            } else {
                System.out.println("Failed to add expense. Please try again.");
            }
        }
    }

    private static void viewExpenses(Connection connection) throws SQLException {
        String selectExpensesSQL = "SELECT e.id, e.description, e.amount, c.category_name, e.date " +
                "FROM expenses e " +
                "JOIN categories c ON e.category_id = c.id";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectExpensesSQL)) {

            System.out.println("Expense List:");
            System.out.println("ID\tDescription\t\tAmount\tCategory\t\tDate");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String description = resultSet.getString("description");
                double amount = resultSet.getDouble("amount");
                String category = resultSet.getString("category_name");
                Date date = resultSet.getDate("date");

                System.out.printf("%d\t%-20s\t%.2f\t%-20s\t%s%n", id, description, amount, category, date);
            }
        }
    }

    private static void updateExpense(Scanner scanner, Connection connection) throws SQLException {
        try {
            viewExpenses(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.print("Enter the expense ID to update: ");
        int expenseId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        if (expenseExists(expenseId, connection)) {
            System.out.print("Enter new expense description: ");
            String newDescription = scanner.nextLine();

            System.out.print("Enter new expense amount: ");
            double newAmount = scanner.nextDouble();

            displayCategories(connection);

            int newCategoryId = selectCategory(scanner, connection);

            System.out.print("Enter new expense date (YYYY-MM-DD): ");
            String newDate = scanner.next();

            String updateExpenseSQL = "UPDATE expenses SET description = ?, amount = ?, category_id = ?, date = ? WHERE id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(updateExpenseSQL)) {
                preparedStatement.setString(1, newDescription);
                preparedStatement.setDouble(2, newAmount);
                preparedStatement.setInt(3, newCategoryId);
                preparedStatement.setString(4, newDate);
                preparedStatement.setInt(5, expenseId);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Expense updated successfully!");
                } else {
                    System.out.println("Failed to update expense. Please try again.");
                }
            }
        } else {
            System.out.println("Expense with ID " + expenseId + " not found.");
        }
    }

    private static void deleteExpense(Scanner scanner, Connection connection) throws SQLException {
        try {
            viewExpenses(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.out.print("Enter the expense ID to delete: ");
        int expenseId = scanner.nextInt();
        scanner.nextLine(); // Consume the newline character

        if (expenseExists(expenseId, connection)) {
            String deleteExpenseSQL = "DELETE FROM expenses WHERE id = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(deleteExpenseSQL)) {
                preparedStatement.setInt(1, expenseId);

                int affectedRows = preparedStatement.executeUpdate();

                if (affectedRows > 0) {
                    System.out.println("Expense deleted successfully!");
                } else {
                    System.out.println("Failed to delete expense. Please try again.");
                }
            }
        } else {
            System.out.println("Expense with ID " + expenseId + " not found.");
        }
    }

    private static boolean expenseExists(int expenseId, Connection connection) throws SQLException {
        String selectExpenseSQL = "SELECT id FROM expenses WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectExpenseSQL)) {
            preparedStatement.setInt(1, expenseId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private static void addNewCategory(Scanner scanner, Connection connection) throws SQLException {
        System.out.print("Enter new category: ");
        String newCategory = scanner.next();

        String insertCategorySQL = "INSERT INTO categories (category_name) VALUES (?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertCategorySQL)) {
            preparedStatement.setString(1, newCategory);

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                System.out.println("New category added successfully!");
            } else {
                System.out.println("Failed to add new category. Please try again.");
            }
        }
    }

    private static void displayCategories(Connection connection) throws SQLException {
        System.out.println("Available Categories:");

        String selectCategoriesSQL = "SELECT * FROM categories";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(selectCategoriesSQL)) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String categoryName = resultSet.getString("category_name");
                System.out.println(id + ". " + categoryName);
            }
        }
    }

    private static int selectCategory(Scanner scanner, Connection connection) {
        while (true) {
            System.out.print("Enter the category ID for the expense,\n if not found required category press 0 to enter new category: ");
            int categoryId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline character
            if (categoryId == 0) {
                try {
                    addNewCategory(scanner, connection);
                    displayCategories(connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } else {
                if (categoryExists(categoryId, connection)) {
                    return categoryId;
                } else {
                    System.out.println("Invalid category ID. Please enter a valid category ID.");
                }
            }
        }
    }

    private static boolean categoryExists(int categoryId, Connection connection) {
        String selectCategorySQL = "SELECT id FROM categories WHERE id = ?";

        try (PreparedStatement preparedStatement = connection.prepareStatement(selectCategorySQL)) {
            preparedStatement.setInt(1, categoryId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
