package com.financemanager.ui;

import com.financemanager.model.User;
import com.financemanager.service.FinanceService;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;

public class LoginView {

    private final Stage stage;
    private final FinanceService service;

    private VBox root;
    private TextField usernameField;
    private PasswordField passwordField;
    private Label messageLabel;

    public LoginView(Stage stage, FinanceService service) {
        this.stage = stage;
        this.service = service;
        buildScreen();
    }

    public VBox getRoot() {
        return root;
    }

    private void buildScreen() {
        root = new VBox();
        root.getStyleClass().add("login-root");
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(22));
        root.setPrefWidth(420);

        VBox card = new VBox(14);
        card.getStyleClass().add("login-card");
        card.setMaxWidth(360);

        Label title = new Label("Personal Finance Manager");
        title.getStyleClass().add("login-title");
        Label subtitle = UiStyle.hint("Log in, or create a new account to get started.");
        subtitle.setWrapText(true);

        usernameField = new TextField();
        usernameField.setPromptText("Username");

        passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        Button loginButton = UiStyle.button("Log In");
        loginButton.setPrefWidth(140);
        loginButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleLogin();
            }
        });

        Button registerButton = UiStyle.lightButton("Register");
        registerButton.setPrefWidth(140);
        registerButton.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleRegister();
            }
        });

        HBox buttons = new HBox(10, loginButton, registerButton);
        buttons.setAlignment(Pos.CENTER);

        messageLabel = new Label("");
        messageLabel.getStyleClass().add("error-text");
        messageLabel.setWrapText(true);
        messageLabel.setAlignment(Pos.CENTER);
        messageLabel.setMinHeight(30);

        passwordField.setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                handleLogin();
            }
        });

        card.getChildren().addAll(title, subtitle, usernameField, passwordField,
                buttons, messageLabel);
        root.getChildren().add(card);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter both a username and a password.");
            return;
        }

        try {
            User user = service.login(username, password);
            if (user == null) {
                messageLabel.setText("Wrong username or password. Please try again.");
                return;
            }
            openMainWindow(user);
        } catch (SQLException e) {
            messageLabel.setText("Problem with the database: " + e.getMessage());
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please choose a username and a password.");
            return;
        }
        if (username.length() < 3) {
            messageLabel.setText("The username needs at least 3 characters.");
            return;
        }

        try {
            if (service.usernameExists(username)) {
                messageLabel.setText("That username is already taken. Please choose another one.");
                return;
            }
            service.register(username, password);
            User user = service.login(username, password);
            openMainWindow(user);
        } catch (SQLException e) {
            messageLabel.setText("Problem with the database: " + e.getMessage());
        }
    }

    private void openMainWindow(User user) {
        MainWindow mainWindow = new MainWindow(stage, service, user);
        stage.setTitle("Personal Finance Manager - " + user.getUsername());
        stage.setScene(mainWindow.getScene());
        stage.setResizable(true);
    }
}
