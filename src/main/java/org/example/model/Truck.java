package org.example.model;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;
import java.util.Timer;
import java.util.TimerTask;

public class Truck {
    //lista cegiel zaladowanych
    private final List<Integer> loadedBricks = new ArrayList<>();
    private static int size;
    private int actual = 0;
    //wlasciwosci javafx do monitorowania stanu ciezarowki
    private final IntegerProperty currentWeight = new SimpleIntegerProperty(0);
    private final IntegerProperty maxCapacity = new SimpleIntegerProperty(0);
    private final BooleanProperty informationProperty = new SimpleBooleanProperty(false);
    private volatile boolean running = true;
    private Timer animationTimer;
    private Timer updateTimer;

    public Truck(int size) {
        super();
        Truck.size = size;
        maxCapacity.set(size);
        startUpdateTimer();
        System.out.println("Truck utworzony o ladownosci: " + size);
    }
    private void startUpdateTimer() {
        updateTimer = new Timer(true); //daemon timer
        updateTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (running) {
                    updateProperties();
                }
            }
        }, 0, 100); //aktualizacja co 100ms
    }

    private void updateProperties() {
        if (!running) return;
        Platform.runLater(() -> {
            if (running) {
                currentWeight.set(actual);
                maxCapacity.set(size);
            }
        });
    }

    public void stopTruck() {
        running = false;
        cleanupResources();
    }

    private void cleanupResources() {
        //anulowanie zaplanowanych zadan timerow
        if (animationTimer != null) {
            animationTimer.cancel();
            animationTimer = null;
        }
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
        System.out.println("Truck - zasoby wyczyszczone");
    }

    public synchronized boolean isFull() {
        return actual == size;
    }

    public synchronized boolean canLoad(int brick) {
        return actual + brick <= size;
    }

    public synchronized void loadBrick(int brick) {
        if (actual + brick > size) {
            throw new IllegalStateException("Proba przeladowania ciezarowki");
        }
        loadedBricks.add(brick);
        actual += brick;
        System.out.println("Zaladowano cegle " + brick + "kg. Stan ciezarowki: " + actual + "/" + size);
        //aktualizacja wlasciwosci po zaladowaniu cegly
        if (running) {
            updateProperties();
        }
        //sprawdzenie czy ciezarowka jest pelna
        if (isFull()) {
            System.out.println("Ciezarowka pelna! Rozpoczynanie rozladunku...");
            //animacja pelnej ciezarowki
            if (running) {
                Platform.runLater(() -> {
                    if (running) {
                        informationProperty.set(true);
                        if (animationTimer != null) {
                            animationTimer.cancel();
                        }
                        animationTimer = new Timer();
                        animationTimer.schedule(
                                new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (running) {
                                            Platform.runLater(() -> {
                                                if (running) {
                                                    informationProperty.set(false);
                                                }
                                            });
                                        }
                                    }
                                },
                                1000
                        );
                    }
                });
            }
        }
    }
    public synchronized void unload() {
        if (actual == 0) {
            return; //juz pusta
        }
        System.out.println("ROZLADOWANO ciezarowke. Bylo: " + actual + "kg, " + loadedBricks.size() + " cegiel");
        loadedBricks.clear();   //usuwa wszystkie cegly z ciezarowki
        actual = 0;            //resetuje aktualna mase

        //aktualizacja jesli aplikacja dziala
        if (running) {
            updateProperties();
        }
    }

    //metody monitorowania
    public IntegerProperty currentWeightProperty() {
        return currentWeight;
    }

    public IntegerProperty maxCapacityProperty() {
        return maxCapacity;
    }


}