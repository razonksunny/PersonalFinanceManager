package com.financemanager.model;

import java.time.LocalDate;

public abstract class Transaction {

    private int id;
    private int userId;
    private long amountCents;
    private String category;
    private String description;
    private LocalDate date;

    public Transaction() {
    }

    public Transaction(int id, int userId, long amountCents, String category,
                       String description, LocalDate date) {
        this.id = id;
        this.userId = userId;
        this.amountCents = amountCents;
        this.category = category;
        this.description = description;
        this.date = date;
    }

    public abstract String getType();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(long amountCents) {
        this.amountCents = amountCents;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
