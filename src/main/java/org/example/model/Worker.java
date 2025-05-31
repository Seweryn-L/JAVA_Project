package org.example.model;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.example.app.AppMain;

public class Worker extends Thread {
    private final ConveyorBelt belt = AppMain.belt;
    private final int brickMass;
    private final BooleanProperty information = new SimpleBooleanProperty(false);
    private final int time;
    private volatile boolean brickAdded = false;
    private volatile boolean running = true;

    public Worker(String name, int brickMass, int time) {
        super(name);
        this.brickMass = brickMass;
        this.time = time;
        setDaemon(true);
    }

    public BooleanProperty informationProperty() {
        return information;
    }

    public void stopWorker() {
        running = false;
        interrupt();
    }

    @Override
    public void run() {
        try {
            while (running && !Thread.currentThread().isInterrupted()) {
                if (running && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(time);
                } else {
                    break;
                }

                brickAdded = false;

                try {
                    //proba dodania cegly na tasme
                    belt.addBrick(brickMass);
                    brickAdded = true;
                    System.out.println(getName() + " dodal cegle " + brickMass + "kg na tasme");
                    //animacja dodania cegly
                    if (!Thread.currentThread().isInterrupted() && running) {
                        Platform.runLater(() -> {
                            if (brickAdded && running) {
                                //notyfikacja workera
                                informationProperty().set(true);

                                //animacja tasmy
                                belt.triggerBeltAnimation();
                                //reset notyfikacji po krotkiej chwili
                                Thread resetThread = new Thread(() -> {
                                    try {
                                        Thread.sleep(500);
                                        if (Platform.isFxApplicationThread()) {
                                            informationProperty().set(false);
                                        } else {
                                            Platform.runLater(() -> informationProperty().set(false));
                                        }
                                    } catch (InterruptedException ex) {
                                        Thread.currentThread().interrupt();
                                    }
                                });
                                resetThread.setDaemon(true);
                                resetThread.start();
                            }
                        });
                    }

                } catch (InterruptedException e) {
                    //worker zostal przerwany
                    Thread.currentThread().interrupt();
                    running = false;
                    break;
                } catch (Exception e) {
                    if (running) {
                        System.err.println(getName() + " problem z dodaniem cegly: " + e.getMessage());
                    }
                    Thread.currentThread().interrupt();
                    running = false;
                }
                //oczekiwanie przed nastepna cegla
                if (running && !Thread.currentThread().isInterrupted()) {
                    Thread.sleep(time);
                } else {
                    break;
                }
            }
        } catch (InterruptedException e) {
            //normalnie przerwany
        } catch (Exception e) {
            System.err.println(getName() + " nieoczekiwany blad: " + e.getMessage());
        } finally {
            System.out.println(getName() + " zostal zakonczony");
            running = false;
        }
    }

    @Override
    public void interrupt() {
        running = false;
        super.interrupt();
    }
}