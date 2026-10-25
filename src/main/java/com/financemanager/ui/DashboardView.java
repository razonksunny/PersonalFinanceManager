package com.financemanager.ui;

import com.financemanager.model.Budget;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.service.FinanceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.SQLException;
import java.util.List;

public class DashboardView {

    private final FinanceService service;
    private final User user;

    private VBox root;
    private Label incomeValue;
    private Label expenseValue;
    private Label balanceValue;
    private Label messageLabel;
    private TableView<Transaction> recentTable;
    private VBox alertsBox;

    public DashboardView(FinanceService service, User user) {
        this.service = service;
        this.user = user;
        buildScreen();
        refreshDashboard();
    }

    public VBox getRoot() {
        return root;
    }

    private void buildScreen() {
        root = new VBox(18);
        root.getStyleClass().add("page-root");
        root.setPadding(new Insets(5));

        Label title = UiStyle.title("Dashboard");

        Button refreshButton = UiStyle.lightButton("Refresh");
        refreshButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                refreshDashboard();
            }
        });

        HBox titleRow = new HBox(12, title);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        titleRow.getChildren().addAll(spacer, refreshButton);

        incomeValue = UiStyle.valueLabel();
        expenseValue = UiStyle.valueLabel();
        balanceValue = UiStyle.valueLabel();

        VBox incomeCard = UiStyle.card("Total income", incomeValue, "value-good");
        VBox expenseCard = UiStyle.card("Total expenses", expenseValue, "value-danger");
        VBox balanceCard = UiStyle.card("Current balance", balanceValue, "value-neutral");

        HBox cards = new HBox(16, incomeCard, expenseCard, balanceCard);
        cards.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);
        HBox.setHgrow(balanceCard, Priority.ALWAYS);

        Label recentTitle = new Label("Recent transactions");
        recentTitle.getStyleClass().add("section-title");

        recentTable = new TableView<Transaction>();
        recentTable.getColumns().add(makeColumn("Date", 1));
        recentTable.getColumns().add(makeColumn("Description", 2));
        recentTable.getColumns().add(makeAmountColumn());
        recentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        recentTable.setPrefHeight(220);
        recentTable.setMinHeight(220);
        recentTable.setPlaceholder(new Label("No transactions yet."));

        VBox recentPanel = new VBox(6, recentTitle, recentTable);
        recentPanel.getStyleClass().add("panel");
        HBox.setHgrow(recentPanel, Priority.ALWAYS);

        Label alertsTitle = new Label("Budget alerts");
        alertsTitle.getStyleClass().add("section-title");

        alertsBox = new VBox(8);

        VBox alertsPanel = new VBox(6, alertsTitle, alertsBox);
        alertsPanel.getStyleClass().add("panel");
        alertsPanel.setPrefWidth(320);
        alertsPanel.setMinWidth(280);

        HBox panels = new HBox(16, recentPanel, alertsPanel);
        panels.setAlignment(Pos.TOP_LEFT);

        Label explanation = UiStyle.hint(
                "The balance is the total income minus the total expenses of all your transactions.");
        explanation.setWrapText(true);

        messageLabel = new Label("");
        messageLabel.getStyleClass().add("section-hint");

        root.getChildren().addAll(titleRow, cards, panels, explanation, messageLabel);
    }

    private void refreshDashboard() {
        try {
            long income = service.getTotalIncome(user);
            long expenses = service.getTotalExpense(user);
            long balance = income - expenses;

            incomeValue.setText(service.formatMoney(income));
            expenseValue.setText(service.formatMoney(expenses));
            balanceValue.setText(service.formatMoney(balance));
            UiStyle.setValueTone(balanceValue, balance >= 0);

            showRecentTransactions();
            showBudgetAlerts();

            messageLabel.setText("Last updated: " + service.getToday());
        } catch (SQLException e) {
            messageLabel.setText("Could not read the numbers: " + e.getMessage());
        }
    }

    private void showRecentTransactions() {
        List<Transaction> transactions = service.filterTransactions(user, "", "", "");
        int count = Math.min(5, transactions.size());
        recentTable.getItems().setAll(transactions.subList(0, count));
    }

    private void showBudgetAlerts() throws SQLException {
        alertsBox.getChildren().clear();
        List<Budget> budgets = service.getBudgetsForMonth(user, service.getCurrentMonth());

        int shown = 0;
        for (int i = 0; i < budgets.size(); i++) {
            Budget budget = budgets.get(i);
            if (Budget.STATUS_OK.equals(budget.getStatus())) {
                continue;
            }
            alertsBox.getChildren().add(makeAlertRow(budget));
            shown++;
        }

        if (shown == 0) {
            Label hint = new Label(budgets.isEmpty()
                    ? "No budgets set for this month."
                    : "Every budget is on track.");
            hint.getStyleClass().add("section-hint");
            alertsBox.getChildren().add(hint);
        }
    }

    private VBox makeAlertRow(Budget budget) {
        Label category = new Label(budget.getCategory());
        category.getStyleClass().add("body-text");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label status = new Label(budget.getStatus());
        status.getStyleClass().add("status-cell");
        if (Budget.STATUS_OVER.equals(budget.getStatus())) {
            status.getStyleClass().add("status-over");
        } else {
            status.getStyleClass().add("status-warn");
        }

        HBox top = new HBox(8, category, spacer, status);
        top.setAlignment(Pos.CENTER_LEFT);

        Label spent = new Label("Spent " + service.formatMoney(budget.getSpentCents())
                + " of " + service.formatMoney(budget.getLimitCents()));
        spent.getStyleClass().add("section-hint");

        VBox row = new VBox(2, top, spent);
        row.getStyleClass().add("alert-row");
        return row;
    }

    private TableColumn<Transaction, String> makeColumn(String title, final int columnNumber) {
        TableColumn<Transaction, String> column = new TableColumn<Transaction, String>(title);
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> rowData) {
                Transaction transaction = rowData.getValue();
                if (columnNumber == 1) {
                    return new SimpleStringProperty(transaction.getDate().toString());
                }
                return new SimpleStringProperty(transaction.getDescription());
            }
        });
        return column;
    }

    private TableColumn<Transaction, String> makeAmountColumn() {
        TableColumn<Transaction, String> column = new TableColumn<Transaction, String>("Amount");
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> rowData) {
                Transaction transaction = rowData.getValue();
                if ("Income".equals(transaction.getType())) {
                    return new SimpleStringProperty("+" + service.formatMoney(transaction.getAmountCents()));
                }
                return new SimpleStringProperty("-" + service.formatMoney(transaction.getAmountCents()));
            }
        });
        column.setCellFactory(new Callback<TableColumn<Transaction, String>, TableCell<Transaction, String>>() {
            public TableCell<Transaction, String> call(TableColumn<Transaction, String> columnData) {
                return new TableCell<Transaction, String>() {
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().remove("amount-in");
                        getStyleClass().remove("amount-out");
                        if (empty || item == null) {
                            setText(null);
                            return;
                        }
                        setText(item);
                        if (item.startsWith("+")) {
                            getStyleClass().add("amount-in");
                        } else {
                            getStyleClass().add("amount-out");
                        }
                    }
                };
            }
        });
        return column;
    }
}
