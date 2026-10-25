package com.financemanager.ui;

import com.financemanager.model.Category;
import com.financemanager.model.Expense;
import com.financemanager.model.Income;
import com.financemanager.model.Transaction;
import com.financemanager.model.User;
import com.financemanager.service.FinanceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Callback;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TransactionsView {

    private static final String ALL = "All";

    private final FinanceService service;
    private final User user;

    private VBox root;
    private TextField amountField;
    private ChoiceBox<String> typeBox;
    private ChoiceBox<String> formCategoryBox;
    private TextField descriptionField;
    private DatePicker datePicker;
    private TextField searchField;
    private ChoiceBox<String> filterTypeBox;
    private ChoiceBox<String> filterCategoryBox;
    private TableView<Transaction> table;
    private Label messageLabel;
    private Label summaryLabel;

    public TransactionsView(FinanceService service, User user) {
        this.service = service;
        this.user = user;
        buildScreen();
        refreshTable();
    }

    public VBox getRoot() {
        return root;
    }

    private void buildScreen() {
        root = new VBox(10);
        root.getStyleClass().add("page-root");
        root.setPadding(new Insets(5));

        Label title = UiStyle.title("Transactions");

        searchField = new TextField();
        searchField.setPromptText("Search anything: description, category, type, date or amount");
        searchField.setPrefWidth(220);

        filterTypeBox = new ChoiceBox<String>();
        filterTypeBox.getItems().addAll(ALL, "Income", "Expense");
        filterTypeBox.setValue(ALL);
        filterTypeBox.setPrefWidth(105);

        filterCategoryBox = new ChoiceBox<String>();
        filterCategoryBox.getItems().add(ALL);
        filterCategoryBox.getItems().addAll(Category.getAllCategories());
        filterCategoryBox.setValue(ALL);
        filterCategoryBox.setPrefWidth(125);

        Button clearFilterButton = UiStyle.lightButton("Clear");
        clearFilterButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                clearFilter();
            }
        });

        HBox filterRow = new HBox(10, searchField,
                UiStyle.body("Type"), filterTypeBox,
                UiStyle.body("Category"), filterCategoryBox,
                clearFilterButton);
        filterRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(searchField, Priority.ALWAYS);

        ChangeListener<String> filterListener = new ChangeListener<String>() {
            public void changed(ObservableValue<? extends String> observable,
                                String oldValue, String newValue) {
                refreshTable();
            }
        };
        searchField.textProperty().addListener(filterListener);
        filterTypeBox.valueProperty().addListener(filterListener);
        filterCategoryBox.valueProperty().addListener(filterListener);

        VBox filterPanel = new VBox(filterRow);
        filterPanel.getStyleClass().add("panel");

        amountField = new TextField();
        amountField.setPromptText("e.g. 12.50");
        amountField.setPrefWidth(110);

        typeBox = new ChoiceBox<String>();
        typeBox.getItems().addAll("Income", "Expense");
        typeBox.setValue("Expense");
        typeBox.setPrefWidth(105);

        formCategoryBox = new ChoiceBox<String>();
        formCategoryBox.getItems().addAll(Category.getExpenseCategories());
        formCategoryBox.setValue("Food");
        formCategoryBox.setPrefWidth(125);

        descriptionField = new TextField();
        descriptionField.setPromptText("What was this about?");

        datePicker = new DatePicker();
        datePicker.setValue(LocalDate.now());
        datePicker.setPrefWidth(125);

        Button addButton = UiStyle.button("Add");
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleAdd();
            }
        });

        Button updateButton = UiStyle.lightButton("Update");
        updateButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleUpdate();
            }
        });

        Button deleteButton = UiStyle.lightButton("Delete");
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleDelete();
            }
        });

        Button clearFormButton = UiStyle.lightButton("Clear form");
        clearFormButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                clearForm();
            }
        });

        HBox topFields = new HBox(12,
                field("Amount", amountField),
                field("Type", typeBox),
                field("Category", formCategoryBox),
                field("Date", datePicker));
        topFields.setAlignment(Pos.BOTTOM_LEFT);

        VBox descriptionGroup = field("Description", descriptionField);
        HBox.setHgrow(descriptionGroup, Priority.ALWAYS);

        HBox actions = new HBox(8, addButton, updateButton, deleteButton, clearFormButton);
        actions.setAlignment(Pos.BOTTOM_RIGHT);

        HBox bottomFields = new HBox(12, descriptionGroup, actions);
        bottomFields.setAlignment(Pos.BOTTOM_LEFT);

        VBox formPanel = new VBox(8, topFields, bottomFields);
        formPanel.getStyleClass().add("panel");

        typeBox.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                fillCategoryChoices(typeBox.getValue());
            }
        });

        table = new TableView<Transaction>();
        table.getColumns().add(makeColumn("Date", 1));
        table.getColumns().add(makeColumn("Type", 2));
        table.getColumns().add(makeColumn("Category", 3));
        table.getColumns().add(makeColumn("Description", 4));
        table.getColumns().add(makeColumn("Amount", 5));
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("No transactions yet. Add one above."));

        table.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<Transaction>() {
            public void changed(ObservableValue<? extends Transaction> observable,
                                Transaction oldValue, Transaction newValue) {
                fillForm(newValue);
            }
        });

        VBox tableBox = new VBox();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableBox.getChildren().add(table);

        summaryLabel = new Label("");
        summaryLabel.getStyleClass().add("summary-label");
        messageLabel = new Label("");
        messageLabel.getStyleClass().add("section-hint");
        messageLabel.setWrapText(true);

        root.getChildren().addAll(title, filterPanel, formPanel, tableBox,
                summaryLabel, messageLabel);
    }

    private VBox field(String labelText, Region control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(4, label, control);
    }

    private TableColumn<Transaction, String> makeColumn(String title, final int columnNumber) {
        TableColumn<Transaction, String> column = new TableColumn<Transaction, String>(title);
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Transaction, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Transaction, String> rowData) {
                return new SimpleStringProperty(getCellText(rowData.getValue(), columnNumber));
            }
        });
        return column;
    }

    private String getCellText(Transaction transaction, int columnNumber) {
        if (columnNumber == 1) {
            return transaction.getDate().toString();
        }
        if (columnNumber == 2) {
            return transaction.getType();
        }
        if (columnNumber == 3) {
            return transaction.getCategory();
        }
        if (columnNumber == 4) {
            return transaction.getDescription();
        }
        return service.formatMoney(transaction.getAmountCents());
    }

    private void fillCategoryChoices(String type) {
        formCategoryBox.getItems().clear();
        if ("Income".equals(type)) {
            formCategoryBox.getItems().addAll(Category.getIncomeCategories());
        } else {
            formCategoryBox.getItems().addAll(Category.getExpenseCategories());
        }
        formCategoryBox.setValue(formCategoryBox.getItems().get(0));
    }

    private void refreshTable() {

        String filterType = filterTypeBox.getValue();
        if (ALL.equals(filterType)) {
            filterType = "";
        }
        String filterCategory = filterCategoryBox.getValue();
        if (ALL.equals(filterCategory)) {
            filterCategory = "";
        }

        List<Transaction> filtered = service.filterTransactions(user,
                searchField.getText().trim(),
                filterType,
                filterCategory);
        table.getItems().setAll(filtered);
        updatePlaceholder();
        showSummary(filtered);
    }

    private void updatePlaceholder() {
        String search = searchField.getText().trim();
        boolean typeFiltered = !ALL.equals(filterTypeBox.getValue());
        boolean categoryFiltered = !ALL.equals(filterCategoryBox.getValue());

        if (search.isEmpty() && !typeFiltered && !categoryFiltered) {
            table.setPlaceholder(new Label("No transactions yet. Add one above."));
        } else if (!search.isEmpty()) {
            table.setPlaceholder(new Label("No transactions match \"" + search + "\"."));
        } else {
            table.setPlaceholder(new Label("No transactions match the selected filters."));
        }
    }

    private void showSummary(List<Transaction> filtered) {
        long income = 0;
        long expenses = 0;
        for (int i = 0; i < filtered.size(); i++) {
            Transaction transaction = filtered.get(i);
            if ("Income".equals(transaction.getType())) {
                income = income + transaction.getAmountCents();
            } else {
                expenses = expenses + transaction.getAmountCents();
            }
        }
        summaryLabel.setText("Showing " + filtered.size() + " transaction(s)   |   "
                + "Income: " + service.formatMoney(income)
                + "   |   Expenses: " + service.formatMoney(expenses)
                + "   |   Balance: " + service.formatMoney(income - expenses));
    }

    private void handleAdd() {
        long cents = FinanceService.parseCents(amountField.getText());
        if (cents < 0) {
            messageLabel.setText("Please enter an amount bigger than zero.");
            return;
        }
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            messageLabel.setText("Please enter a short description.");
            return;
        }

        String type = typeBox.getValue();
        String category = formCategoryBox.getValue();
        LocalDate date = datePicker.getValue();

        Transaction transaction;
        if ("Income".equals(type)) {
            transaction = new Income(0, user.getId(), cents, category, description, date);
        } else {
            transaction = new Expense(0, user.getId(), cents, category, description, date);
        }

        try {
            service.addTransaction(user, transaction);
            refreshTable();
            if (table.getItems().contains(transaction)) {
                messageLabel.setText("Transaction added.");
            } else {
                messageLabel.setText("Transaction added, but the current filter hides it.");
            }
            clearForm();
        } catch (SQLException e) {
            messageLabel.setText("Could not add the transaction: " + e.getMessage());
        }
    }

    private void handleUpdate() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please click a row in the table first.");
            return;
        }
        long cents = FinanceService.parseCents(amountField.getText());
        if (cents < 0) {
            messageLabel.setText("Please enter an amount bigger than zero.");
            return;
        }
        String description = descriptionField.getText().trim();
        if (description.isEmpty()) {
            messageLabel.setText("Please enter a short description.");
            return;
        }

        String type = typeBox.getValue();
        String category = formCategoryBox.getValue();
        LocalDate date = datePicker.getValue();

        Transaction changed;
        if ("Income".equals(type)) {
            changed = new Income(selected.getId(), user.getId(), cents, category, description, date);
        } else {
            changed = new Expense(selected.getId(), user.getId(), cents, category, description, date);
        }

        try {
            service.updateTransaction(user, changed);
            messageLabel.setText("Transaction updated.");
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            messageLabel.setText("Could not update the transaction: " + e.getMessage());
        }
    }

    private void handleDelete() {
        Transaction selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please click a row in the table first.");
            return;
        }
        if (!UiStyle.askYesNo("Delete transaction",
                "Delete the transaction \"" + selected.getDescription() + "\"? The amount "
                + service.formatMoney(selected.getAmountCents()) + " will be removed.")) {
            return;
        }
        try {
            service.deleteTransaction(user, selected.getId());
            messageLabel.setText("Transaction deleted.");
            refreshTable();
            clearForm();
        } catch (SQLException e) {
            messageLabel.setText("Could not delete the transaction: " + e.getMessage());
        }
    }

    private void fillForm(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        amountField.setText(String.format("%.2f", transaction.getAmountCents() / 100.0));
        typeBox.setValue(transaction.getType());
        fillCategoryChoices(transaction.getType());
        if (formCategoryBox.getItems().contains(transaction.getCategory())) {
            formCategoryBox.setValue(transaction.getCategory());
        }
        descriptionField.setText(transaction.getDescription());
        datePicker.setValue(transaction.getDate());
    }

    private void clearForm() {
        amountField.clear();
        descriptionField.clear();
        datePicker.setValue(LocalDate.now());
        table.getSelectionModel().clearSelection();
    }

    private void clearFilter() {
        searchField.clear();
        filterTypeBox.setValue(ALL);
        filterCategoryBox.setValue(ALL);
        refreshTable();
    }
}
