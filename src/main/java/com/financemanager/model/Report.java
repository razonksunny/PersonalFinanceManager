package com.financemanager.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class Report {

    private String month;
    private long totalIncomeCents;
    private long totalExpenseCents;
    private long balanceCents;
    private Map<String, Long> expenseByCategory = new LinkedHashMap<String, Long>();

    public Report() {
    }

    public Report(String month) {
        this.month = month;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getTotalIncomeCents() {
        return totalIncomeCents;
    }

    public void setTotalIncomeCents(long totalIncomeCents) {
        this.totalIncomeCents = totalIncomeCents;
    }

    public long getTotalExpenseCents() {
        return totalExpenseCents;
    }

    public void setTotalExpenseCents(long totalExpenseCents) {
        this.totalExpenseCents = totalExpenseCents;
    }

    public long getBalanceCents() {
        return balanceCents;
    }

    public void setBalanceCents(long balanceCents) {
        this.balanceCents = balanceCents;
    }

    public Map<String, Long> getExpenseByCategory() {
        return expenseByCategory;
    }

    public void setExpenseByCategory(Map<String, Long> expenseByCategory) {
        this.expenseByCategory = expenseByCategory;
    }
}
