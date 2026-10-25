package com.financemanager.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class User {

    private int id;
    private String username;
    private String passwordHash;
    private List<Transaction> transactions = new ArrayList<Transaction>();
    private List<Budget> budgets = new ArrayList<Budget>();

    public User() {
    }

    public User(int id, String username, String passwordHash) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder text = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                text.append(String.format("%02x", hash[i]));
            }
            return text.toString();
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }

    public boolean checkPassword(String password) {
        return passwordHash.equals(hashPassword(password));
    }

    public void addTransaction(Transaction transaction) {
        transactions.add(transaction);
    }

    public void updateTransaction(Transaction transaction) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId() == transaction.getId()) {
                transactions.set(i, transaction);
                return;
            }
        }
        transactions.add(transaction);
    }

    public void removeTransaction(int transactionId) {
        for (int i = 0; i < transactions.size(); i++) {
            if (transactions.get(i).getId() == transactionId) {
                transactions.remove(i);
                return;
            }
        }
    }

    public void addBudget(Budget budget) {
        budgets.add(budget);
    }

    public void updateBudget(Budget budget) {
        for (int i = 0; i < budgets.size(); i++) {
            if (budgets.get(i).getId() == budget.getId()) {
                budgets.set(i, budget);
                return;
            }
        }
        budgets.add(budget);
    }

    public void removeBudget(int budgetId) {
        for (int i = 0; i < budgets.size(); i++) {
            if (budgets.get(i).getId() == budgetId) {
                budgets.remove(i);
                return;
            }
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    public List<Budget> getBudgets() {
        return budgets;
    }

    public void setBudgets(List<Budget> budgets) {
        this.budgets = budgets;
    }
}
