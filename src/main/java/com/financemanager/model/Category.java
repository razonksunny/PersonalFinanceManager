package com.financemanager.model;

import java.util.ArrayList;
import java.util.List;

public class Category {

    public static final String[] EXPENSE_CATEGORIES = {
        "Food", "Transport", "Education", "Shopping", "Health", "Other"
    };

    public static final String[] INCOME_CATEGORIES = {
        "Salary", "Bonus", "Gift", "Other"
    };

    public static List<String> getExpenseCategories() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < EXPENSE_CATEGORIES.length; i++) {
            list.add(EXPENSE_CATEGORIES[i]);
        }
        return list;
    }

    public static List<String> getIncomeCategories() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < INCOME_CATEGORIES.length; i++) {
            list.add(INCOME_CATEGORIES[i]);
        }
        return list;
    }

    public static List<String> getAllCategories() {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < EXPENSE_CATEGORIES.length; i++) {
            list.add(EXPENSE_CATEGORIES[i]);
        }
        for (int i = 0; i < INCOME_CATEGORIES.length; i++) {
            if (!list.contains(INCOME_CATEGORIES[i])) {
                list.add(INCOME_CATEGORIES[i]);
            }
        }
        return list;
    }
}
