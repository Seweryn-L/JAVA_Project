package org.example.config;

public class AppConfig {
    //tasma
    private int beltCountMax;
    private int beltWeightMax;

    //ciezarowka
    private int truckCapacity;

    //pracownicy
    private WorkerConfig worker1;
    private WorkerConfig worker2;
    private WorkerConfig worker3;

    //gettery i setter
    public int getBeltCountMax() { return beltCountMax; }
    public void setBeltCountMax(int beltCountMax) { this.beltCountMax = beltCountMax; }

    public int getBeltWeightMax() { return beltWeightMax; }
    public void setBeltWeightMax(int beltWeightMax) { this.beltWeightMax = beltWeightMax; }

    public int getTruckCapacity() { return truckCapacity; }
    public void setTruckCapacity(int truckCapacity) { this.truckCapacity = truckCapacity; }

    public WorkerConfig getWorker1() { return worker1; }
    public void setWorker1(WorkerConfig worker1) { this.worker1 = worker1; }

    public WorkerConfig getWorker2() { return worker2; }
    public void setWorker2(WorkerConfig worker2) { this.worker2 = worker2; }

    public WorkerConfig getWorker3() { return worker3; }
    public void setWorker3(WorkerConfig worker3) { this.worker3 = worker3; }

    //konfiguracja pracownika
    public static class WorkerConfig {
        private int brickMass;
        private int intervalMin;
        private int intervalMax;

        public int getBrickMass() { return brickMass; }
        public void setBrickMass(int brickMass) { this.brickMass = brickMass; }

        public int getIntervalMin() { return intervalMin; }
        public void setIntervalMin(int intervalMin) { this.intervalMin = intervalMin; }

        public int getIntervalMax() { return intervalMax; }
        public void setIntervalMax(int intervalMax) { this.intervalMax = intervalMax; }
    }

}
