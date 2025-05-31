package org.example.model;

import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

//tasma przenosnikowa symuluje system produkcyjny
//transportuje cegly z maksymalnymi ograniczeniami liczby i masy
//cegly zjezdzaja co 3 sekundy
public class ConveyorBelt extends Thread {
    //cegla ktora jest na tasme
    private static class BrickOnBelt {
        final int mass;
        final long addTime;

        BrickOnBelt(int mass, long addTime) {
            this.mass = mass;
            this.addTime = addTime;
        }
    }

    //lista cegiel w transporcie
    private final List<BrickOnBelt> bricksInTransit = Collections.synchronizedList(new ArrayList<>());
    //executor do zarzadzania zaplanowanymi zadaniami
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    //semafor ograniczajacy maksymalna liczbe i mase cegiel
    private Semaphore countSemaphore;
    private Semaphore weightSemaphore;

    //maksymalne parametry tasmy
    private final int countMax;   //K - maksymalna liczba cegiel
    private final int weightMax;  //M - maksymalna masa w kg
    //biezacy stan tasmy
    private volatile int currentWeight = 0;  //aktualna suma mas
    private volatile int currentCount = 0;   //aktualna liczba cegiel

    //referencja do ciezarowki
    private Truck truck;

    //flaga kontrolujaca dzialanie tasmy
    private volatile boolean running = true;
    //javafx properties do integracji z gui
    private final IntegerProperty weightMaxProperty = new SimpleIntegerProperty();
    private final IntegerProperty countMaxProperty = new SimpleIntegerProperty();
    private final IntegerProperty brickCountProperty = new SimpleIntegerProperty(0);
    private final IntegerProperty brickWeightProperty = new SimpleIntegerProperty(0);

    //wlasciwosc do animacji
    private final BooleanProperty informationProperty = new SimpleBooleanProperty(false);
    public final BooleanProperty unloadProperty = new SimpleBooleanProperty(false);

    public ConveyorBelt(int K, int M) {
        super("ConveyorBelt");
        this.countMax = K;
        this.weightMax = M;
        this.countSemaphore = new Semaphore(K, true);
        this.weightSemaphore = new Semaphore(M, true);
        setDaemon(true);
        //inicjalizacjia javafx
        Platform.runLater(() -> {
            countMaxProperty.set(countMax);
            weightMaxProperty.set(weightMax);
            brickCountProperty.set(0);
            brickWeightProperty.set(0);
        });
    }

    //ustawia referencje do ciezarowki
    public void setTruck(Truck truck) {
        this.truck = truck;
    }

    //wyzwala animacje tasmy w gui
    public void triggerBeltAnimation() {
        Platform.runLater(() -> {
            informationProperty.set(true);
            scheduler.schedule(() -> {
                Platform.runLater(() -> informationProperty.set(false));
            }, 100, TimeUnit.MILLISECONDS);
        });
    }

    //dodaje cegle na tasme, blokujaca metoda
    public void addBrick(int mass) throws InterruptedException {
        if (!running) {
            throw new InterruptedException("ConveyorBelt is stopped");
        }
        //rezerwacja miejsca na tasme
        countSemaphore.acquire();
        weightSemaphore.acquire(mass);
        //dodanie cegly z aktualnym czasem
        long currentTime = System.currentTimeMillis();
        BrickOnBelt brick = new BrickOnBelt(mass, currentTime);
        synchronized (this) {
            currentWeight += mass;
            currentCount++;
            bricksInTransit.add(brick);
            System.out.println("Dodano cegle na tasme: " + mass + "kg. Stan tasmy: " +
                    currentWeight + "/" + weightMax + " (" + currentCount + "/" + countMax + ")");
        }
        //zaplanowanie zjezdzania cegly po 3 sekundach
        scheduler.schedule(() -> {
            removeBrickFromBelt(brick);
        }, 3, TimeUnit.SECONDS);
        //aktualizacja gui
        Platform.runLater(() -> {
            brickCountProperty.set(currentCount);
            brickWeightProperty.set(currentWeight);
        });
    }

    //usuwa cegle z tasmy i probuje zaladowac do ciezarowki
    private void removeBrickFromBelt(BrickOnBelt brick) {
        if (!running) return;
        synchronized (this) {
            if (!bricksInTransit.remove(brick)) {
                return;
            }
            currentWeight -= brick.mass;
            currentCount--;
            System.out.println("Cegla " + brick.mass + "kg zjezdza z tasmy");
        }
        //proba zaladowania do ciezarowki
        boolean loaded = false;
        if (truck != null) {
            synchronized (truck) {
                if (truck.canLoad(brick.mass)) {
                    truck.loadBrick(brick.mass);
                    loaded = true;
                    System.out.println("Zaladowano cegle " + brick.mass + "kg do ciezarowki");
                }
            }
            //rozladowanie ciezarowki jesli nie udalo sie zaladowac
            if (!loaded) {
                System.out.println("Nie mozna zaladowac cegly - rozladowywanie ciezarowki");
                try {
                    Platform.runLater(() -> {
                        unloadProperty.set(true);
                    });
                    Thread.sleep(50);
                    truck.unload();
                    Thread.sleep(100);
                    Platform.runLater(() -> {
                        unloadProperty.set(false);
                    });
                    Thread.sleep(50);
                    //ponowna proba zaladowania
                    synchronized (truck) {
                        if (truck.canLoad(brick.mass)) {
                            truck.loadBrick(brick.mass);
                            System.out.println("Zaladowano cegle " + brick.mass + "kg po rozladunku");
                        } else {
                            System.err.println("BLAD: Nie mozna zaladowac cegly nawet po rozladunku!");
                        }
                    }
                } catch (InterruptedException e) {
                    Platform.runLater(() -> {
                        unloadProperty.set(false);
                    });
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        //zwolnienie semaforow
        countSemaphore.release();
        weightSemaphore.release(brick.mass);
        //aktualizacja gui
        Platform.runLater(() -> {
            brickCountProperty.set(currentCount);
            brickWeightProperty.set(currentWeight);
        });
    }

    //glowna petla watku
    @Override
    public void run() {
        System.out.println("ConveyorBelt uruchomiony");
        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                running = false;
                break;
            }
        }
        System.out.println("ConveyorBelt zakonczony");
    }
    //gettery dla javafx properties
    public IntegerProperty weightMaxProperty() {
        return weightMaxProperty;
    }
    public IntegerProperty countMaxProperty() {
        return countMaxProperty;
    }
    public IntegerProperty brickCountProperty() {
        return brickCountProperty;
    }
    public IntegerProperty brickWeightProperty() {
        return brickWeightProperty;
    }
    public BooleanProperty informationProperty() {
        return informationProperty;
    }
}