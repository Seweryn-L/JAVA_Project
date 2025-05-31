package org.example.app;

import org.example.config.AppConfig;
import org.example.config.ConfigLoader;
import org.example.gui.MainWindow;
import org.example.model.ConveyorBelt;
import org.example.model.Truck;
import org.example.model.Worker;
import java.util.Random;

public class AppMain {
    public static Worker worker1;
    public static Worker worker2;
    public static Worker worker3;
    public static Truck truck;
    public static ConveyorBelt belt;
    static Random rand = new Random();

    public static void initializeActors(AppConfig config) {
        belt = new ConveyorBelt(config.getBeltCountMax(), config.getBeltWeightMax());

        truck = new Truck(config.getTruckCapacity());
        belt.setTruck(truck);
        belt.start();

        int interval1 = config.getWorker1().getIntervalMin() +
                rand.nextInt(config.getWorker1().getIntervalMax() - config.getWorker1().getIntervalMin() + 1);
        worker1 = new Worker("P1", config.getWorker1().getBrickMass(), interval1);

        int interval2 = config.getWorker2().getIntervalMin() +
                rand.nextInt(config.getWorker2().getIntervalMax() - config.getWorker2().getIntervalMin() + 1);
        worker2 = new Worker("P2", config.getWorker2().getBrickMass(), interval2);

        int interval3 = config.getWorker3().getIntervalMin() +
                rand.nextInt(config.getWorker3().getIntervalMax() - config.getWorker3().getIntervalMin() + 1);
        worker3 = new Worker("P3", config.getWorker3().getBrickMass(), interval3);
    }


    public static void main(String[] args) {
        AppConfig config = ConfigLoader.loadConfig();
        MainWindow.launch(MainWindow.class, args);
    }
}