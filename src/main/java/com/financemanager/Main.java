package com.financemanager;

import com.financemanager.db.DatabaseManager;
import com.financemanager.service.FinanceService;
import com.financemanager.ui.LoginView;
import com.financemanager.ui.UiStyle;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private DatabaseManager database;

    @Override
    public void start(Stage stage) {
        database = new DatabaseManager();
        try {
            database.connect();
        } catch (Exception e) {
            UiStyle.showError("Could not open the database file: " + e.getMessage());
            return;
        }

        FinanceService service = new FinanceService(database);
        LoginView loginView = new LoginView(stage, service);

        Scene scene = new Scene(loginView.getRoot(), 420, 430);
        UiStyle.apply(scene);

        stage.setTitle("Personal Finance Manager");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @Override
    public void stop() {
        if (database != null) {
            database.close();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
