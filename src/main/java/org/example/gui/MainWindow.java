package org.example.gui;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.example.app.AppMain;
import javafx.scene.control.Label;
import org.example.config.AppConfig;
import org.example.config.ConfigLoader;
import org.example.model.ConveyorBelt;
import javafx.scene.layout.VBox;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;


public class MainWindow extends Application {
    private final Background background = new Background();
    private final StackPane root = new StackPane();
    private WorkersGui workersGui;
    private Stage primaryStageRef;
    private ConveyorBelt belt;
    private final CountDownLatch uiInitLatch = new CountDownLatch(1);
    private VBox truckBox; //referencja do truckBox jako pole klasy


    private void initializeUI(Stage primaryStage) {
        try {
            //dodanie tla
            root.getChildren().add(background.getView());

            //tworzenie boxow z informacjami
            VBox infoBox = createInfoBox();
            this.truckBox = createTruckBox();

            AnchorPane backgroundPane = background.getView();
            backgroundPane.getChildren().add(truckBox);
            AnchorPane.setBottomAnchor(truckBox, 70.0);
            AnchorPane.setRightAnchor(truckBox, 150.0);

            //tworzenie scene z CSS
            Scene scene = new Scene(root, 900, 400);

            //dodanie stylowania CSS
            try {
                String cssPath = Objects.requireNonNull(getClass().getResource("/style.css")).toExternalForm();
                scene.getStylesheets().add(cssPath);
            } catch (Exception e) {
                System.err.println("Failed to load CSS: " + e.getMessage());
            }

            //konfiguracja i wyswietlenie stage
            primaryStage.setScene(scene);
            primaryStage.setTitle("Brick Transport - JavaFX");
            primaryStage.setResizable(false);

            //dodanie info boxes do root
            root.getChildren().add(infoBox);
            StackPane.setAlignment(infoBox, Pos.TOP_CENTER);

            //obsluga zamkniecia okna
            primaryStage.setOnCloseRequest(event -> {
                event.consume();
                stop();
            });

            //wyswietlenie stage
            primaryStage.show();

            //sygnalizacja ze UI jest gotowe
            Platform.runLater(() -> uiInitLatch.countDown());

        } catch (Exception e) {
            System.err.println("Error initializing UI: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private void initializeWorkersGui() {
        try {
            //inicjalizacja WorkersGui
            workersGui = new WorkersGui(primaryStageRef);
            //ustawienie listenerow przed uruchomieniem watkow
            setupWorkerListeners();

        } catch (Exception e) {
            System.err.println("Error initializing WorkersGui: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize WorkersGui", e);
        }
    }

    private void initializeActors() {
        try {
            AppConfig config = ConfigLoader.loadConfig();
            //tworzenie pracownikow
            AppMain.initializeActors(config);

        } catch (Exception e) {
            System.err.println("Error initializing actors: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initialize actors", e);
        }
    }

    private void startThreads() {
        //uruchamianie z opoznieniem aby zapewnic ze UI jest gotowe
        Platform.runLater(() -> {
            try {
                Thread.sleep(1000); //opoznienie 1s

                //uruchomienie watkow
                if (AppMain.worker1 != null) AppMain.worker1.start();
                if (AppMain.worker2 != null) AppMain.worker2.start();
                if (AppMain.worker3 != null) AppMain.worker3.start();

                System.out.println("All threads started successfully");

            } catch (Exception e) {
                System.err.println("Error starting threads: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private VBox createInfoBox() {
        //tworzenie etykiet UI
        Label constantLabel = new Label("Stan Tasmy");
        constantLabel.getStyleClass().add("title-label");

        Label countLabel = new Label();
        countLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("Liczba \uD83E\uDDF1 %d / %d",
                                belt.brickCountProperty().get(),
                                belt.countMaxProperty().get()),
                        belt.brickCountProperty(),
                        belt.countMaxProperty()
                )
        );

        Label weightLabel = new Label();
        weightLabel.textProperty().bind(
                Bindings.createStringBinding(
                        () -> String.format("Masa: \uD83E\uDDF1 %d / %d",
                                belt.brickWeightProperty().get(),
                                belt.weightMaxProperty().get()),
                        belt.brickWeightProperty(),
                        belt.weightMaxProperty()
                )
        );

        //tworzenie info box
        VBox infoBox = new VBox(5);
        infoBox.getStyleClass().add("info-box");
        infoBox.getChildren().addAll(
                constantLabel,
                countLabel,
                weightLabel
        );

        infoBox.setMaxWidth(200);
        infoBox.setMaxHeight(50);

        return infoBox;
    }

    public VBox createTruckBox() {
        Label constantLabel = new Label("Stan Ciezarowki");
        constantLabel.getStyleClass().add("title-label");

        Label weightLabel = new Label();
        //bind zostanie ustawiony gdy Truck jest zainicjalizowany
        Platform.runLater(() -> {
            if (AppMain.truck != null) {
                weightLabel.textProperty().bind(
                        Bindings.createStringBinding(
                                () -> String.format("Masa \uD83E\uDDF1 %d / %d",
                                        AppMain.truck.currentWeightProperty().get(),
                                        AppMain.truck.maxCapacityProperty().get()),
                                AppMain.truck.currentWeightProperty(),
                                AppMain.truck.maxCapacityProperty()
                        )
                );
            }
        });

        //tworzenie truck box
        VBox truckBox = new VBox(5);
        truckBox.getStyleClass().add("truck-label-box");
        truckBox.getChildren().addAll(
                constantLabel,
                weightLabel
        );

        truckBox.setMaxWidth(200);
        truckBox.setMaxHeight(50);

        return truckBox;
    }

    private void setupWorkerListeners() {
        //sprawdzenie czy WorkersGui jest zainicjalizowane
        if (workersGui == null) {
            System.err.println("WorkersGui not initialized!");
            workersGui = new WorkersGui(primaryStageRef);
        }

        //aktualizacja referencji stage
        workersGui.updateStage(primaryStageRef);

        //ustawienie listenerow dla powiadomien pracownikow
        if (AppMain.worker1 != null) {
            AppMain.worker1.informationProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Platform.runLater(() -> workersGui.showNotification(true, "P1"));
                }
            });
        }

        if (AppMain.worker2 != null) {
            AppMain.worker2.informationProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Platform.runLater(() -> workersGui.showNotification(true, "P2"));
                }
            });
        }

        if (AppMain.worker3 != null) {
            AppMain.worker3.informationProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Platform.runLater(() -> workersGui.showNotification(true, "P3"));
                }
            });
        }

        //ustawienie listenera dla animacji ciezarowki
        if (AppMain.truck != null) {
            AppMain.belt.unloadProperty.addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Platform.runLater(() -> {
                        //wywolanie animacji ciezarowki
                        background.moveTruck();
                    });
                }
            });
        }
    }

    public Stage getPrimaryStageRef() {
        return primaryStageRef;
    }

    public void setPrimaryStageRef(Stage primaryStageRef) {
        this.primaryStageRef = primaryStageRef;
        if (workersGui != null) {
            workersGui.updateStage(primaryStageRef);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            //zapisanie referencji do primary stage
            this.primaryStageRef = primaryStage;

            //1. inicjalizacja aktorow
            initializeActors();
            this.belt = AppMain.belt;
            belt.informationProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    Platform.runLater(() -> background.moveBrick());
                }
            });

            System.out.println("Starting MainWindow setup");

            //2. inicjalizacja glownego UI
            initializeUI(primaryStage);

            //3. inicjalizacja WorkersGui
            initializeWorkersGui();

            //4. uruchomienie watkow
            startThreads();

        } catch (Exception e) {
            System.err.println("Error initializing MainWindow: " + e.getMessage());
            e.printStackTrace();
            Platform.exit();
        }
    }
    @Override
    public void stop() {
        //czyszczenie przy zamykaniu aplikacji
        try {
            System.out.println("Zatrzymywanie aplikacji...");

            //zatrzymywanie watkow pracownikow
            if (AppMain.worker1 != null) {
                AppMain.worker1.stopWorker();
                System.out.println("Worker1 zatrzymany");
            }

            if (AppMain.worker2 != null) {
                AppMain.worker2.stopWorker();
                System.out.println("Worker2 zatrzymany");
            }

            if (AppMain.worker3 != null) {
                AppMain.worker3.stopWorker();
                System.out.println("Worker3 zatrzymany");
            }

            //zatrzymywanie ciezarowki
            if (AppMain.truck != null) {
                AppMain.truck.stopTruck();
                System.out.println("Truck zatrzymany");
            }

            //czas na zakonczenie watkow
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            System.out.println("Wszystkie watki zatrzymane");

        } catch (Exception e) {
            System.err.println("Error stopping threads: " + e.getMessage());
            e.printStackTrace();
        } finally {
            //upewnienie sie ze aplikacja sie zamknie
            Platform.exit();
            System.exit(0);
        }
    }
}