package org.example.gui;

import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Background {
    private final AnchorPane root;
    private ImageView smallBrickImageView;
    private ImageView truckImageView;

    public Background() {

        //ladowanie obrazow
        Image truckImage = loadImage("/truck.png");
        truckImageView = createImageView(truckImage, 400, true);

        Image truckConstImage = loadImage("/truck.png");
        ImageView truckConstImageView = createImageView(truckConstImage, 400, true);

        Image conveyorBeltImage = loadImage("/conveyorBelt.png");
        ImageView conveyorBeltImageView = new ImageView(conveyorBeltImage);
        conveyorBeltImageView.setFitWidth(1200);
        conveyorBeltImageView.setFitHeight(150);
        conveyorBeltImageView.setPreserveRatio(true);

        Image worker1Image = loadImage("/worker1.png");
        ImageView worker1ImageView = createImageView(worker1Image, 100, true);

        Image worker2Image = loadImage("/worker2.png");
        ImageView worker2ImageView = createImageView(worker2Image, 100, true);

        Image worker3Image = loadImage("/worker3.png");
        ImageView worker3ImageView = createImageView(worker3Image, 125, true);

        Image brickImage = loadImage("/bricks.png");
        ImageView brickImageView = createImageView(brickImage, 300, true);

        //tworzenie animowanej cegly poczatkowo ukryta
        Image smallBrickImage = loadImage("/oneBrick.png");
        smallBrickImageView = createImageView(smallBrickImage, 100, true);
        smallBrickImageView.setVisible(false);

        //tworzenie glownego kontenera, wazna kolejnosc warstw
        //elementy dodane wczesniej beda pod elementami dodanymi pozniej
        root = new AnchorPane(
                conveyorBeltImageView,    //warstwa dolna
                worker1ImageView,        //pracownicy
                worker2ImageView,
                worker3ImageView,
                brickImageView,          //statyczne cegly
                smallBrickImageView,     //animowane cegly
                truckImageView,           //ciezarowka na wierzchu
                truckConstImageView
        );
        //ustawienie tla
        setBackgroundImage();
        //pozycje elementow
        AnchorPane.setBottomAnchor(smallBrickImageView, 60.0);
        AnchorPane.setLeftAnchor(smallBrickImageView, 340.0);

        AnchorPane.setBottomAnchor(truckImageView, -30.0);
        AnchorPane.setRightAnchor(truckImageView, 20.0);

        AnchorPane.setBottomAnchor(truckConstImageView, -30.0);
        AnchorPane.setRightAnchor(truckConstImageView, 20.0);

        AnchorPane.setBottomAnchor(conveyorBeltImageView, 0.0);
        AnchorPane.setLeftAnchor(conveyorBeltImageView, 300.0);

        AnchorPane.setBottomAnchor(worker1ImageView, 0.0);
        AnchorPane.setLeftAnchor(worker1ImageView, 10.0);

        AnchorPane.setBottomAnchor(worker2ImageView, 0.0);
        AnchorPane.setLeftAnchor(worker2ImageView, 130.0);

        AnchorPane.setBottomAnchor(worker3ImageView, 0.0);
        AnchorPane.setLeftAnchor(worker3ImageView, 250.0);

        AnchorPane.setBottomAnchor(brickImageView, -100.0);
        AnchorPane.setLeftAnchor(brickImageView, 10.0);
    }


    private final List<ImageView> activeBricks = new ArrayList<>();

    public void moveBrick() {
        //utworzenie nowego imageview dla kazdej cegly
        Image smallBrickImage = loadImage("/oneBrick.png");
        ImageView brick = createImageView(smallBrickImage, 80, true);
        brick.setVisible(true);

        //pozycja poczatkowa
        AnchorPane.setBottomAnchor(brick, 80.0);
        AnchorPane.setLeftAnchor(brick, 340.0);

        //wstawienie cegly w odpowiednim miejscu w hierarchii warstw
        int truckIndex = root.getChildren().indexOf(truckImageView);
        if (truckIndex != -1) {
            root.getChildren().add(truckIndex, brick); //wstaw przed ciezarowka
        } else {
            root.getChildren().add(brick); //fallback
        }

        activeBricks.add(brick);

        //animacja
        TranslateTransition transition = new TranslateTransition(Duration.seconds(3), brick);
        transition.setToX(150);
        transition.setOnFinished(e -> {
            root.getChildren().remove(brick);
            activeBricks.remove(brick);
        });
        transition.play();
    }

    public void moveTruck() {
        //animacja w prawo
        TranslateTransition moveRight = new TranslateTransition(Duration.seconds(2.5), truckImageView);
        moveRight.setToX(400);

        //animacja powrotu
        TranslateTransition moveLeft = new TranslateTransition(Duration.seconds(2.5), truckImageView);
        moveLeft.setToX(0);

        //sekwencja animacji
        SequentialTransition sequence = new SequentialTransition(moveRight, moveLeft);
        sequence.play();
    }

    private Image loadImage(String path) {
        try {
            return new Image(Objects.requireNonNull(getClass().getResource(path)).toExternalForm());
        } catch (Exception e) {
            System.err.println("Blad ladowania obrazu: " + path);
            return null;
        }
    }

    private ImageView createImageView(Image image, double width, boolean preserveRatio) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(width);
        imageView.setPreserveRatio(preserveRatio);
        return imageView;
    }

    private void setBackgroundImage() {
        try {
            Image backgroundImage = loadImage("/background.jpg");
            BackgroundImage background = new BackgroundImage(
                    backgroundImage,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );
            root.setBackground(new javafx.scene.layout.Background(background));
        } catch (Exception e) {
            System.err.println("Nie mozna zaladowac tla: " + e.getMessage());
            root.setStyle("-fx-background-color: #f0f0f0;");
        }
    }

    public AnchorPane getView() {
        return root;
    }
}