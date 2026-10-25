package com.financemanager.ui;

import com.financemanager.model.Report;
import com.financemanager.model.User;
import com.financemanager.service.FinanceService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportsView {

    private final FinanceService service;
    private final User user;

    private VBox root;
    private DatePicker monthPicker;
    private Label incomeValue;
    private Label expenseValue;
    private Label balanceValue;
    private TableView<String> categoryTable;
    private VBox chartBox;
    private Label messageLabel;

    public ReportsView(FinanceService service, User user) {
        this.service = service;
        this.user = user;
        buildScreen();
        generateReport();
    }

    public VBox getRoot() {
        return root;
    }

    private void buildScreen() {
        root = new VBox(14);
        root.getStyleClass().add("page-root");
        root.setPadding(new Insets(5));

        Label title = UiStyle.title("Reports");

        monthPicker = new DatePicker();
        monthPicker.setValue(LocalDate.now());
        monthPicker.setPrefWidth(140);

        Button generateButton = UiStyle.button("Generate report");
        generateButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                generateReport();
            }
        });

        HBox monthRow = new HBox(8, UiStyle.body("Month:"), monthPicker, generateButton);
        monthRow.setAlignment(Pos.CENTER_LEFT);

        incomeValue = UiStyle.valueLabel();
        expenseValue = UiStyle.valueLabel();
        balanceValue = UiStyle.valueLabel();

        VBox incomeCard = UiStyle.card("Total income", incomeValue, "value-good");
        VBox expenseCard = UiStyle.card("Total expenses", expenseValue, "value-danger");
        VBox balanceCard = UiStyle.card("Balance", balanceValue, "value-neutral");

        HBox cards = new HBox(16, incomeCard, expenseCard, balanceCard);
        cards.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(incomeCard, Priority.ALWAYS);
        HBox.setHgrow(expenseCard, Priority.ALWAYS);
        HBox.setHgrow(balanceCard, Priority.ALWAYS);

        categoryTable = new TableView<String>();
        TableColumn<String, String> categoryColumn = new TableColumn<String, String>("Category");
        categoryColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<String, String> rowData) {
                return new SimpleStringProperty(rowData.getValue());
            }
        });
        TableColumn<String, String> amountColumn = new TableColumn<String, String>("Amount");
        amountColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<String, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<String, String> rowData) {
                return new SimpleStringProperty(service.formatMoney(amountFor(rowData.getValue())));
            }
        });
        categoryTable.getColumns().add(categoryColumn);
        categoryTable.getColumns().add(amountColumn);
        categoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        categoryTable.setPlaceholder(new Label("No expenses for this month."));
        categoryTable.setPrefHeight(254);
        categoryTable.setMinWidth(400);
        categoryTable.setPrefWidth(420);

        chartBox = new VBox(6);
        chartBox.getStyleClass().add("chart-box");
        chartBox.setPrefWidth(400);
        HBox.setHgrow(chartBox, Priority.ALWAYS);

        HBox middleRow = new HBox(16, categoryTable, chartBox);
        middleRow.setAlignment(Pos.TOP_LEFT);

        messageLabel = new Label("");
        messageLabel.getStyleClass().add("section-hint");
        messageLabel.setWrapText(true);

        root.getChildren().addAll(title, monthRow, cards, middleRow, messageLabel);
    }

    private String getSelectedMonth() {
        LocalDate date = monthPicker.getValue();
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private void generateReport() {
        try {
            Report report = service.buildReport(user, getSelectedMonth());

            incomeValue.setText(service.formatMoney(report.getTotalIncomeCents()));
            expenseValue.setText(service.formatMoney(report.getTotalExpenseCents()));
            balanceValue.setText(service.formatMoney(report.getBalanceCents()));
            UiStyle.setValueTone(balanceValue, report.getBalanceCents() >= 0);

            Map<String, Long> byCategory = report.getExpenseByCategory();
            List<String> names = new ArrayList<String>();
            for (String category : byCategory.keySet()) {
                names.add(category);
            }
            categoryTable.getItems().setAll(names);

            drawChart(byCategory);
            messageLabel.setText("Report of " + report.getMonth() + ".");
        } catch (SQLException e) {
            messageLabel.setText("Could not read the report: " + e.getMessage());
        }
    }

    private Map<String, Long> currentCategoryAmounts = new LinkedHashMap<String, Long>();

    private long amountFor(String category) {
        Long amount = currentCategoryAmounts.get(category);
        if (amount == null) {
            return 0;
        }
        return amount.longValue();
    }

    private void drawChart(Map<String, Long> byCategory) {
        currentCategoryAmounts = byCategory;
        chartBox.getChildren().clear();
        chartBox.getChildren().add(UiStyle.heading("Expenses by category"));

        if (byCategory.isEmpty()) {
            StackPane empty = new StackPane(
                    UiStyle.hint("No expenses were recorded for this month."));
            VBox.setVgrow(empty, Priority.ALWAYS);
            chartBox.getChildren().add(empty);
            return;
        }

        long biggest = 0;
        for (String category : byCategory.keySet()) {
            long amount = byCategory.get(category).longValue();
            if (amount > biggest) {
                biggest = amount;
            }
        }

        HBox bars = new HBox(10);
        bars.setAlignment(Pos.BOTTOM_LEFT);

        HBox names = new HBox(10);
        names.setAlignment(Pos.TOP_LEFT);

        for (String category : byCategory.keySet()) {
            long amount = byCategory.get(category).longValue();
            double height = 0;
            if (biggest > 0) {
                height = 104.0 * amount / biggest;
            }

            Rectangle bar = new Rectangle(38, height);
            bar.getStyleClass().add("chart-bar");

            StackPane plot = new StackPane(bar);
            plot.setAlignment(Pos.BOTTOM_CENTER);
            plot.setMinSize(58, 104);
            plot.setPrefSize(58, 104);

            Label nameLabel = new Label(category);
            nameLabel.getStyleClass().add("chart-name");
            nameLabel.setMaxWidth(Double.MAX_VALUE);
            nameLabel.setAlignment(Pos.CENTER);

            Label valueLabel = new Label(service.formatMoney(amount));
            valueLabel.getStyleClass().add("chart-value");
            valueLabel.setMaxWidth(Double.MAX_VALUE);
            valueLabel.setAlignment(Pos.CENTER);

            VBox nameBox = new VBox(2, nameLabel, valueLabel);
            nameBox.setPrefWidth(58);
            nameBox.setAlignment(Pos.TOP_CENTER);

            bars.getChildren().add(plot);
            names.getChildren().add(nameBox);
        }

        Region baseline = new Region();
        baseline.getStyleClass().add("chart-baseline");

        VBox chartBody = new VBox(6, bars, baseline, names);
        HBox centeredChart = new HBox(chartBody);
        centeredChart.setAlignment(Pos.CENTER);
        VBox.setVgrow(centeredChart, Priority.ALWAYS);
        chartBox.getChildren().add(centeredChart);
    }
}
