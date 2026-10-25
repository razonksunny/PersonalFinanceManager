package com.financemanager.db;

import com.financemanager.model.Budget;
import com.financemanager.model.Expense;
import com.financemanager.model.Income;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private static final String FILE_NAME = "finance.db";
    private static final String URL = "jdbc:sqlite:" + FILE_NAME;

    private Connection connection;

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL);
        createTables();
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            System.out.println("Could not close the database: " + e.getMessage());
        }
    }

    private void createTables() throws SQLException {
        String users = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "username TEXT NOT NULL UNIQUE, "
                + "password_hash TEXT NOT NULL)";

        String transactions = "CREATE TABLE IF NOT EXISTS transactions ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "amount_cents INTEGER NOT NULL, "
                + "type TEXT NOT NULL, "
                + "category TEXT NOT NULL, "
                + "description TEXT NOT NULL, "
                + "date TEXT NOT NULL)";

        String budgets = "CREATE TABLE IF NOT EXISTS budgets ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "user_id INTEGER NOT NULL, "
                + "category TEXT NOT NULL, "
                + "limit_cents INTEGER NOT NULL, "
                + "month TEXT NOT NULL, "
                + "UNIQUE (user_id, category, month))";

        Statement statement = connection.createStatement();
        statement.executeUpdate(users);
        statement.executeUpdate(transactions);
        statement.executeUpdate(budgets);
        statement.close();
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT id FROM users WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        ResultSet results = statement.executeQuery();
        boolean exists = results.next();
        results.close();
        statement.close();
        return exists;
    }

    public void insertUser(String username, String passwordHash) throws SQLException {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        statement.setString(2, passwordHash);
        statement.executeUpdate();
        statement.close();
    }

    public User findUserByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash FROM users WHERE username = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, username);
        ResultSet results = statement.executeQuery();
        User user = null;
        if (results.next()) {
            user = new User(results.getInt("id"),
                            results.getString("username"),
                            results.getString("password_hash"));
        }
        results.close();
        statement.close();
        return user;
    }

    public void insertTransaction(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO transactions "
                + "(user_id, amount_cents, type, category, description, date) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, transaction.getUserId());
        statement.setLong(2, transaction.getAmountCents());
        statement.setString(3, transaction.getType());
        statement.setString(4, transaction.getCategory());
        statement.setString(5, transaction.getDescription());
        statement.setString(6, transaction.getDate().toString());
        statement.executeUpdate();
        ResultSet keys = statement.getGeneratedKeys();
        if (keys.next()) {
            transaction.setId(keys.getInt(1));
        }
        keys.close();
        statement.close();
    }

    public void updateTransaction(Transaction transaction) throws SQLException {
        String sql = "UPDATE transactions SET amount_cents = ?, type = ?, category = ?, "
                + "description = ?, date = ? WHERE id = ? AND user_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setLong(1, transaction.getAmountCents());
        statement.setString(2, transaction.getType());
        statement.setString(3, transaction.getCategory());
        statement.setString(4, transaction.getDescription());
        statement.setString(5, transaction.getDate().toString());
        statement.setInt(6, transaction.getId());
        statement.setInt(7, transaction.getUserId());
        statement.executeUpdate();
        statement.close();
    }

    public void deleteTransaction(int userId, int transactionId) throws SQLException {
        String sql = "DELETE FROM transactions WHERE id = ? AND user_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, transactionId);
        statement.setInt(2, userId);
        statement.executeUpdate();
        statement.close();
    }

    public List<Transaction> findTransactionsByUser(int userId) throws SQLException {
        String sql = "SELECT id, amount_cents, type, category, description, date "
                + "FROM transactions WHERE user_id = ? ORDER BY date DESC, id DESC";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, userId);
        ResultSet results = statement.executeQuery();
        List<Transaction> list = new ArrayList<Transaction>();
        while (results.next()) {
            int id = results.getInt("id");
            long amountCents = results.getLong("amount_cents");
            String type = results.getString("type");
            String category = results.getString("category");
            String description = results.getString("description");
            LocalDate date = LocalDate.parse(results.getString("date"));

            if ("Income".equals(type)) {
                list.add(new Income(id, userId, amountCents, category, description, date));
            } else {
                list.add(new Expense(id, userId, amountCents, category, description, date));
            }
        }
        results.close();
        statement.close();
        return list;
    }

    public List<Budget> findBudgetsByUser(int userId) throws SQLException {
        String sql = "SELECT id, category, limit_cents, month FROM budgets WHERE user_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, userId);
        ResultSet results = statement.executeQuery();
        List<Budget> list = new ArrayList<Budget>();
        while (results.next()) {
            list.add(new Budget(results.getInt("id"),
                               userId,
                               results.getString("category"),
                               results.getLong("limit_cents"),
                               results.getString("month")));
        }
        results.close();
        statement.close();
        return list;
    }

    public Budget findBudget(int userId, String category, String month) throws SQLException {
        String sql = "SELECT id, category, limit_cents, month FROM budgets "
                + "WHERE user_id = ? AND category = ? AND month = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, userId);
        statement.setString(2, category);
        statement.setString(3, month);
        ResultSet results = statement.executeQuery();
        Budget budget = null;
        if (results.next()) {
            budget = new Budget(results.getInt("id"),
                                userId,
                                results.getString("category"),
                                results.getLong("limit_cents"),
                                results.getString("month"));
        }
        results.close();
        statement.close();
        return budget;
    }

    public void insertBudget(Budget budget) throws SQLException {
        String sql = "INSERT INTO budgets (user_id, category, limit_cents, month) "
                + "VALUES (?, ?, ?, ?)";
        PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, budget.getUserId());
        statement.setString(2, budget.getCategory());
        statement.setLong(3, budget.getLimitCents());
        statement.setString(4, budget.getMonth());
        statement.executeUpdate();
        ResultSet keys = statement.getGeneratedKeys();
        if (keys.next()) {
            budget.setId(keys.getInt(1));
        }
        keys.close();
        statement.close();
    }

    public void updateBudget(Budget budget) throws SQLException {
        String sql = "UPDATE budgets SET category = ?, limit_cents = ?, month = ? "
                + "WHERE id = ? AND user_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, budget.getCategory());
        statement.setLong(2, budget.getLimitCents());
        statement.setString(3, budget.getMonth());
        statement.setInt(4, budget.getId());
        statement.setInt(5, budget.getUserId());
        statement.executeUpdate();
        statement.close();
    }

    public void saveBudget(Budget budget) throws SQLException {
        Budget existing = findBudget(budget.getUserId(), budget.getCategory(), budget.getMonth());
        if (existing == null) {
            insertBudget(budget);
        } else {
            budget.setId(existing.getId());
            updateBudget(budget);
        }
    }

    public void deleteBudget(int userId, int budgetId) throws SQLException {
        String sql = "DELETE FROM budgets WHERE id = ? AND user_id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, budgetId);
        statement.setInt(2, userId);
        statement.executeUpdate();
        statement.close();
    }

    public long sumAmount(int userId, String type, String category, String month) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount_cents), 0) FROM transactions WHERE user_id = ?";
        List<Object> values = new ArrayList<Object>();
        values.add(userId);

        if (type != null && !type.isEmpty()) {
            sql = sql + " AND type = ?";
            values.add(type);
        }
        if (category != null && !category.isEmpty()) {
            sql = sql + " AND category = ?";
            values.add(category);
        }
        if (month != null && !month.isEmpty()) {
            sql = sql + " AND date LIKE ?";
            values.add(month + "%");
        }

        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < values.size(); i++) {
            statement.setObject(i + 1, values.get(i));
        }
        ResultSet results = statement.executeQuery();
        long total = 0;
        if (results.next()) {
            total = results.getLong(1);
        }
        results.close();
        statement.close();
        return total;
    }

    public Map<String, Long> sumExpensesByCategory(int userId, String month) throws SQLException {
        String sql = "SELECT category, COALESCE(SUM(amount_cents), 0) FROM transactions "
                + "WHERE user_id = ? AND type = 'Expense' AND date LIKE ? "
                + "GROUP BY category ORDER BY SUM(amount_cents) DESC";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setInt(1, userId);
        statement.setString(2, month + "%");
        ResultSet results = statement.executeQuery();
        Map<String, Long> totals = new LinkedHashMap<String, Long>();
        while (results.next()) {
            totals.put(results.getString("category"), results.getLong(2));
        }
        results.close();
        statement.close();
        return totals;
    }
}
