package com.financemanager.ui;

import com.financemanager.model.Budget;
import com.financemanager.model.Category;
import com.financemanager.model.User;
import com.financemanager.service.FinanceService;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BudgetsView {

    private final FinanceService service;
    private final User user;

    private VBox root;
    private DatePicker monthPicker;
    private ChoiceBox<String> categoryBox;
    private TextField limitField;
    private TableView<Budget> table;
    private Label messageLabel;

    public BudgetsView(FinanceService service, User user) {
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

        Label title = UiStyle.title("Budgets");

        monthPicker = new DatePicker();
        monthPicker.setValue(LocalDate.now());
        monthPicker.setPrefWidth(140);

        categoryBox = new ChoiceBox<String>();
        categoryBox.getItems().addAll(Category.getExpenseCategories());
        categoryBox.setValue("Food");
        categoryBox.setPrefWidth(130);

        limitField = new TextField();
        limitField.setPromptText("e.g. 250.00");
        limitField.setPrefWidth(120);

        Button saveButton = UiStyle.button("Save budget");
        saveButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleSave();
            }
        });

        Button deleteButton = UiStyle.lightButton("Delete");
        deleteButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleDelete();
            }
        });

        Button showButton = UiStyle.lightButton("Show month");
        showButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                refreshTable();
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttons = new HBox(8, saveButton, showButton, deleteButton);
        buttons.setAlignment(Pos.BOTTOM_LEFT);

        HBox formRow = new HBox(12,
                field("Month", monthPicker),
                field("Category", categoryBox),
                field("Limit", limitField),
                spacer, buttons);
        formRow.setAlignment(Pos.BOTTOM_LEFT);

        VBox formPanel = new VBox(formRow);
        formPanel.getStyleClass().add("panel");

        Label explanation = UiStyle.hint(
                "A budget is the most you want to spend in one category in one month. "
                + "The status column warns you when the limit has been reached.");
        explanation.setWrapText(true);

        table = new TableView<Budget>();
        table.getColumns().add(makeColumn("Category", 1));
        table.getColumns().add(makeColumn("Month", 2));
        table.getColumns().add(makeColumn("Limit", 3));
        table.getColumns().add(makeColumn("Spent", 4));
        table.getColumns().add(makeColumn("Remaining", 5));
        TableColumn<Budget, String> statusColumn = makeColumn("Status", 6);
        table.getColumns().add(statusColumn);
        colorStatusColumn(statusColumn);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPlaceholder(new Label("No budgets for this month yet. Set one above."));

        VBox tableBox = new VBox();
        VBox.setVgrow(table, Priority.ALWAYS);
        tableBox.getChildren().add(table);

        messageLabel = new Label("");
        messageLabel.getStyleClass().add("section-hint");
        messageLabel.setWrapText(true);

        root.getChildren().addAll(title, formPanel, explanation, tableBox, messageLabel);
    }

    private VBox field(String labelText, Region control) {
        Label label = new Label(labelText);
        label.getStyleClass().add("field-label");
        return new VBox(4, label, control);
    }

    private TableColumn<Budget, String> makeColumn(String title, final int columnNumber) {
        TableColumn<Budget, String> column = new TableColumn<Budget, String>(title);
        column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Budget, String>, ObservableValue<String>>() {
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Budget, String> rowData) {
                return new SimpleStringProperty(getCellText(rowData.getValue(), columnNumber));
            }
        });
        return column;
    }

    private String getCellText(Budget budget, int columnNumber) {
        if (columnNumber == 1) {
            return budget.getCategory();
        }
        if (columnNumber == 2) {
            return budget.getMonth();
        }
        if (columnNumber == 3) {
            return service.formatMoney(budget.getLimitCents());
        }
        if (columnNumber == 4) {
            return service.formatMoney(budget.getSpentCents());
        }
        if (columnNumber == 5) {
            return service.formatMoney(budget.getRemainingCents());
        }
        return budget.getStatus();
    }

    private void colorStatusColumn(TableColumn<Budget, String> column) {
        column.setCellFactory(new Callback<TableColumn<Budget, String>, TableCell<Budget, String>>() {
            public TableCell<Budget, String> call(TableColumn<Budget, String> tableColumn) {
                TableCell<Budget, String> cell = new TableCell<Budget, String>() {
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        getStyleClass().removeAll("status-ok", "status-warn", "status-over");
                        if (empty || item == null) {
                            setText(null);
                            return;
                        }
                        setText(item);
                        if (Budget.STATUS_OVER.equals(item)) {
                            getStyleClass().add("status-over");
                        } else if (Budget.STATUS_REACHED.equals(item)) {
                            getStyleClass().add("status-warn");
                        } else {
                            getStyleClass().add("status-ok");
                        }
                    }
                };
                cell.getStyleClass().add("status-cell");
                return cell;
            }
        });
    }

    private String getSelectedMonth() {
        LocalDate date = monthPicker.getValue();
        return date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
    }

    private void refreshTable() {
        try {
            List<Budget> budgets = service.getBudgetsForMonth(user, getSelectedMonth());
            table.getItems().setAll(budgets);
            messageLabel.setText("Showing the budgets of " + getSelectedMonth() + ".");
        } catch (SQLException e) {
            messageLabel.setText("Could not read the budgets: " + e.getMessage());
        }
    }

    private void handleSave() {
        long cents = FinanceService.parseCents(limitField.getText());
        if (cents < 0) {
            messageLabel.setText("Please enter a limit bigger than zero.");
            return;
        }
        Budget budget = new Budget(0, user.getId(), categoryBox.getValue(), cents, getSelectedMonth());
        try {
            service.saveBudget(user, budget);
            messageLabel.setText("Budget saved for " + budget.getCategory() + " in " + budget.getMonth() + ".");
            limitField.clear();
            refreshTable();
        } catch (SQLException e) {
            messageLabel.setText("Could not save the budget: " + e.getMessage());
        }
    }

    private void handleDelete() {
        Budget selected = table.getSelectionModel().getSelectedItem();
        if (selected == null) {
            messageLabel.setText("Please click a row in the table first.");
            return;
        }
        if (!UiStyle.askYesNo("Delete budget",
                "Delete the budget for " + selected.getCategory() + " in " + selected.getMonth() + "?")) {
            return;
        }
        try {
            service.deleteBudget(user, selected.getId());
            messageLabel.setText("Budget deleted.");
            refreshTable();
        } catch (SQLException e) {
            messageLabel.setText("Could not delete the budget: " + e.getMessage());
        }
    }
}
