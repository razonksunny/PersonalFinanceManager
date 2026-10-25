package com.financemanager.model;

public class Budget {

    public static final String STATUS_OK = "OK";
    public static final String STATUS_REACHED = "LIMIT REACHED";
    public static final String STATUS_OVER = "OVER LIMIT";

    private int id;
    private int userId;
    private String category;
    private long limitCents;
    private String month;
    private long spentCents;

    public Budget() {
    }

    public Budget(int id, int userId, String category, long limitCents, String month) {
        this.id = id;
        this.userId = userId;
        this.category = category;
        this.limitCents = limitCents;
        this.month = month;
    }

    public String getStatus() {
        if (spentCents > limitCents) {
            return STATUS_OVER;
        }
        if (spentCents == limitCents) {
            return STATUS_REACHED;
        }
        return STATUS_OK;
    }

    public boolean isLimitReached() {
        return spentCents >= limitCents;
    }

    public long getRemainingCents() {
        return limitCents - spentCents;
    }

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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getLimitCents() {
        return limitCents;
    }

    public void setLimitCents(long limitCents) {
        this.limitCents = limitCents;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public long getSpentCents() {
        return spentCents;
    }

    public void setSpentCents(long spentCents) {
        this.spentCents = spentCents;
    }
}
