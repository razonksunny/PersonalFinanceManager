package com.financemanager.ui;

import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.util.Optional;

public class UiStyle {

    public static final String STYLESHEET =
            UiStyle.class.getResource("/styles.css").toExternalForm();

    public static void apply(Scene scene) {
        scene.getStylesheets().add(STYLESHEET);
    }

    public static Label title(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("page-title");
        return label;
    }

    public static Label heading(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-title");
        return label;
    }

    public static Label body(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("body-text");
        return label;
    }

    public static Label hint(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("section-hint");
        return label;
    }

    public static Button button(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        return button;
    }

    public static Button lightButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("ghost-button");
        return button;
    }

    public static VBox card(String title, Label valueLabel, String valueClass) {
        VBox card = new VBox(8);
        card.getStyleClass().add("card");
        card.setPrefWidth(230);
        card.setMinWidth(200);
        card.setMaxWidth(Double.MAX_VALUE);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title");

        valueLabel.getStyleClass().addAll("card-value", valueClass);

        card.getChildren().addAll(titleLabel, valueLabel);
        return card;
    }

    public static Label valueLabel() {
        return new Label("0.00");
    }

    public static void setValueTone(Label label, boolean good) {
        label.getStyleClass().removeAll("value-good", "value-danger");
        if (good) {
            label.getStyleClass().add("value-good");
        } else {
            label.getStyleClass().add("value-danger");
        }
    }

    public static void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Personal Finance Manager");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static boolean askYesNo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        ButtonType yes = new ButtonType("Yes", ButtonBar.ButtonData.YES);
        ButtonType no = new ButtonType("No", ButtonBar.ButtonData.NO);
        alert.getButtonTypes().setAll(yes, no);
        Optional<ButtonType> answer = alert.showAndWait();
        if (answer.isPresent() && answer.get() == yes) {
            return true;
        }
        return false;
    }
}
