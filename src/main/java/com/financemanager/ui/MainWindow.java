package com.financemanager.ui;

import com.financemanager.model.User;
import com.financemanager.service.FinanceService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainWindow {

    private final Stage stage;
    private final FinanceService service;
    private final User user;

    private BorderPane root;
    private StackPane content;
    private Button dashboardButton;
    private Button transactionsButton;
    private Button budgetsButton;
    private Button reportsButton;
    private Button activeNavButton;

    public MainWindow(Stage stage, FinanceService service, User user) {
        this.stage = stage;
        this.service = service;
        this.user = user;
        buildWindow();
        showDashboard();
    }

    public Scene getScene() {
        Scene scene = new Scene(root, 1000, 680);
        UiStyle.apply(scene);
        return scene;
    }

    private void buildWindow() {
        root = new BorderPane();
        root.getStyleClass().add("app-root");

        content = new StackPane();
        content.setPadding(new Insets(20));
        root.setCenter(content);

        Label appTitle = new Label("Personal Finance Manager");
        appTitle.getStyleClass().add("app-title");

        Label userLabel = new Label("Logged in as " + user.getUsername());
        userLabel.getStyleClass().add("user-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button logoutButton = UiStyle.lightButton("Logout");
        logoutButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleLogout();
            }
        });

        HBox titleRow = new HBox(10, appTitle, userLabel, spacer, logoutButton);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        dashboardButton = navigationButton("Dashboard");
        dashboardButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showDashboard();
            }
        });

        transactionsButton = navigationButton("Transactions");
        transactionsButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showTransactions();
            }
        });

        budgetsButton = navigationButton("Budgets");
        budgetsButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showBudgets();
            }
        });

        reportsButton = navigationButton("Reports");
        reportsButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                showReports();
            }
        });

        HBox navigation = new HBox(6, dashboardButton, transactionsButton,
                budgetsButton, reportsButton);
        navigation.setAlignment(Pos.CENTER_LEFT);

        VBox topBox = new VBox(12, titleRow, navigation);
        topBox.getStyleClass().add("app-header");
        root.setTop(topBox);
    }

    private Button navigationButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("nav-button");
        button.setPrefWidth(118);
        return button;
    }

    private void setActive(Button button) {
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-active");
        }
        activeNavButton = button;
        if (!button.getStyleClass().contains("nav-active")) {
            button.getStyleClass().add("nav-active");
        }
    }

    private void showScreen(Node screen) {
        content.getChildren().clear();
        content.getChildren().add(screen);
    }

    private void showDashboard() {
        setActive(dashboardButton);
        showScreen(new DashboardView(service, user).getRoot());
    }

    private void showTransactions() {
        setActive(transactionsButton);
        showScreen(new TransactionsView(service, user).getRoot());
    }

    private void showBudgets() {
        setActive(budgetsButton);
        showScreen(new BudgetsView(service, user).getRoot());
    }

    private void showReports() {
        setActive(reportsButton);
        showScreen(new ReportsView(service, user).getRoot());
    }

    private void handleLogout() {
        LoginView loginView = new LoginView(stage, service);
        stage.setTitle("Personal Finance Manager");
        Scene scene = new Scene(loginView.getRoot(), 420, 430);
        UiStyle.apply(scene);
        stage.setScene(scene);
        stage.setResizable(false);
    }
}
