package com.financemanager.service;

import com.financemanager.db.DatabaseManager;
import com.financemanager.model.Budget;
import com.financemanager.model.Report;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FinanceService {

    private DatabaseManager database;

    public FinanceService(DatabaseManager database) {
        this.database = database;
    }

    public DatabaseManager getDatabase() {
        return database;
    }

    public boolean usernameExists(String username) throws SQLException {
        return database.usernameExists(username);
    }

    public void register(String username, String password) throws SQLException {
        database.insertUser(username, User.hashPassword(password));
    }

    public User login(String username, String password) throws SQLException {
        User user = database.findUserByUsername(username);
        if (user == null) {
            return null;
        }
        if (!user.checkPassword(password)) {
            return null;
        }
        loadUserData(user);
        return user;
    }

    public void loadUserData(User user) throws SQLException {
        user.getTransactions().clear();
        user.getBudgets().clear();

        List<Transaction> transactions = database.findTransactionsByUser(user.getId());
        for (int i = 0; i < transactions.size(); i++) {
            user.addTransaction(transactions.get(i));
        }

        List<Budget> budgets = database.findBudgetsByUser(user.getId());
        for (int i = 0; i < budgets.size(); i++) {
            user.addBudget(budgets.get(i));
        }
    }

    public void addTransaction(User user, Transaction transaction) throws SQLException {
        database.insertTransaction(transaction);
        user.addTransaction(transaction);
    }

    public void updateTransaction(User user, Transaction transaction) throws SQLException {
        database.updateTransaction(transaction);
        user.updateTransaction(transaction);
    }

    public void deleteTransaction(User user, int transactionId) throws SQLException {
        database.deleteTransaction(user.getId(), transactionId);
        user.removeTransaction(transactionId);
    }

    public List<Transaction> filterTransactions(User user, String searchText,
                                                String type, String category) {
        List<Transaction> result = new ArrayList<Transaction>();
        List<Transaction> all = user.getTransactions();
        String[] words = getSearchWords(searchText);

        for (int i = 0; i < all.size(); i++) {
            Transaction transaction = all.get(i);
            boolean matchesText = matchesSearch(transaction, words);
            boolean matchesType = true;
            boolean matchesCategory = true;

            if (type != null && !type.isEmpty()) {
                matchesType = transaction.getType().equals(type);
            }
            if (category != null && !category.isEmpty()) {
                matchesCategory = transaction.getCategory().equals(category);
            }

            if (matchesText && matchesType && matchesCategory) {
                result.add(transaction);
            }
        }
        sortByDate(result);
        return result;
    }

    private String[] getSearchWords(String searchText) {
        if (searchText == null) {
            return new String[0];
        }
        String cleaned = searchText.trim().toLowerCase();
        if (cleaned.isEmpty()) {
            return new String[0];
        }
        return cleaned.split("\\s+");
    }

    private boolean matchesSearch(Transaction transaction, String[] words) {
        if (words.length == 0) {
            return true;
        }

        String description = transaction.getDescription();
        if (description == null) {
            description = "";
        }
        String amount = formatMoney(transaction.getAmountCents());
        String haystack = (description + " "
                + transaction.getCategory() + " "
                + transaction.getType() + " "
                + transaction.getDate() + " "
                + monthName(transaction.getDate()) + " "
                + amount + " "
                + amount.replace(",", "") + " "
                + transaction.getAmountCents()).toLowerCase();

        for (int i = 0; i < words.length; i++) {
            if (!haystack.contains(words[i])) {
                return false;
            }
        }
        return true;
    }

    private String monthName(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern("MMMM", Locale.ENGLISH));
    }

    private void sortByDate(List<Transaction> list) {
        Collections.sort(list, new Comparator<Transaction>() {
            public int compare(Transaction first, Transaction second) {
                return second.getDate().compareTo(first.getDate());
            }
        });
    }

    public long getTotalIncome(User user) throws SQLException {
        return database.sumAmount(user.getId(), "Income", null, null);
    }

    public long getTotalExpense(User user) throws SQLException {
        return database.sumAmount(user.getId(), "Expense", null, null);
    }

    public long getBalance(User user) throws SQLException {
        return getTotalIncome(user) - getTotalExpense(user);
    }

    public void saveBudget(User user, Budget budget) throws SQLException {
        database.saveBudget(budget);
        user.updateBudget(budget);
    }

    public void deleteBudget(User user, int budgetId) throws SQLException {
        database.deleteBudget(user.getId(), budgetId);
        user.removeBudget(budgetId);
    }

    public List<Budget> getBudgetsForMonth(User user, String month) throws SQLException {
        List<Budget> result = new ArrayList<Budget>();
        List<Budget> all = user.getBudgets();

        for (int i = 0; i < all.size(); i++) {
            Budget budget = all.get(i);
            if (budget.getMonth().equals(month)) {
                long spent = database.sumAmount(user.getId(), "Expense", budget.getCategory(), month);
                budget.setSpentCents(spent);
                result.add(budget);
            }
        }
        return result;
    }

    public Report buildReport(User user, String month) throws SQLException {
        Report report = new Report(month);

        long income = database.sumAmount(user.getId(), "Income", null, month);
        long expenses = database.sumAmount(user.getId(), "Expense", null, month);
        report.setTotalIncomeCents(income);
        report.setTotalExpenseCents(expenses);
        report.setBalanceCents(income - expenses);

        Map<String, Long> byCategory = database.sumExpensesByCategory(user.getId(), month);
        report.setExpenseByCategory(byCategory);
        return report;
    }

    public static long parseCents(String text) {
        if (text == null) {
            return -1;
        }
        String cleaned = text.trim();
        if (cleaned.isEmpty()) {
            return -1;
        }

        double value;
        try {
            value = Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return -1;
        }
        if (value <= 0) {
            return -1;
        }
        return Math.round(value * 100);
    }

    public String formatMoney(long cents) {
        return String.format("%,.2f", cents / 100.0);
    }

    public LocalDate getToday() {
        return LocalDate.now();
    }

    public String getCurrentMonth() {
        return getToday().format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }
}
