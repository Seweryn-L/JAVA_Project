package org.example.gui;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;

//klasa do wyswietlania powiadomien toast
public class CustomToast {
    private static final Duration FADE_DURATION = Duration.millis(200);

    //wyswietla powiadomienie toast na ekranie
    public static void show(Stage owner, String message, double offsetX, double offsetY, int durationMillis, String workerName) {
        //upewniamy sie ze jestesmy w watku JavaFX
        if (!Platform.isFxApplicationThread()) {
            Platform.runLater(() -> show(owner, message, offsetX, offsetY, durationMillis, workerName));
            return;
        }

        //sprawdzamy poprawnosc stage i scene
        if (owner == null) {
            System.err.println("Cannot show toast: owner is null");
            return;
        }

        if (owner.getScene() == null) {
            System.err.println("Cannot show toast: scene is null");
            return;
        }

        if (owner.getScene().getRoot() == null) {
            System.err.println("Cannot show toast: root is null");
            return;
        }

        try {
            //bezpieczne rzutowanie na Pane
            Pane rootPane;

            if (owner.getScene().getRoot() instanceof Pane) {
                rootPane = (Pane) owner.getScene().getRoot();
            } else {
                System.err.println("Root is not a Pane, it's: " + owner.getScene().getRoot().getClass().getName());
                //fallback - tworzymy overlay
                rootPane = createToastOverlay(owner);
            }

            //tworzymy etykiete toast
            Label toastLabel = new Label(message);
            switch (workerName) {
                case "P1" -> toastLabel.getStyleClass().add("custom-toast-label-p1");
                case "P2" -> toastLabel.getStyleClass().add("custom-toast-label-p2");
                case "P3" -> toastLabel.getStyleClass().add("custom-toast-label-p3");
                case null, default -> toastLabel.getStyleClass().add("custom-toast-label");
            }

            //pozycjonowanie
            toastLabel.setTranslateX(offsetX);
            toastLabel.setTranslateY(offsetY);

            //dodanie do GUI
            rootPane.getChildren().add(toastLabel);

            //animacja fade-in
            FadeTransition fadeIn = new FadeTransition(FADE_DURATION, toastLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            //animacja fade-out
            FadeTransition fadeOut = new FadeTransition(FADE_DURATION, toastLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            //pauza miedzy animacjami
            PauseTransition pause = new PauseTransition(Duration.millis(durationMillis));

            //laczenie animacji
            fadeIn.setOnFinished(e -> pause.play());
            pause.setOnFinished(e -> fadeOut.play());
            fadeOut.setOnFinished(e -> {
                //usuwanie etykiety w watku JavaFX
                Platform.runLater(() -> {
                    rootPane.getChildren().remove(toastLabel);
                });
            });

            //start animacji
            fadeIn.play();
        } catch (Exception e) {
            //logowanie bledu bez crashowania aplikacji
            System.err.println("Error showing toast notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static StackPane createToastOverlay(Stage owner) {
        StackPane overlay = new StackPane();
        overlay.setPrefSize(owner.getWidth(), owner.getHeight());
        overlay.setMouseTransparent(true); //nie blokuje eventow myszy
        overlay.setStyle("-fx-background-color: transparent;");

        //dodanie overlay do scene
        if (owner.getScene().getRoot() instanceof Pane) {
            ((Pane) owner.getScene().getRoot()).getChildren().add(overlay);
        } else {
            System.err.println("Cannot add overlay: root is not a Pane");
            //ostatnia opcja - zamiana root na nowy kontener
            StackPane newRoot = new StackPane();
            newRoot.getChildren().addAll(owner.getScene().getRoot(), overlay);
            owner.getScene().setRoot(newRoot);
        }

        return overlay;
    }
}