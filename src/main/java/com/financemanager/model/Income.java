package com.financemanager.model;

import java.time.LocalDate;

public class Income extends Transaction {

    public Income() {
    }

    public Income(int id, int userId, long amountCents, String category,
                  String description, LocalDate date) {
        super(id, userId, amountCents, category, description, date);
    }

    @Override
    public String getType() {
        return "Income";
    }
}
