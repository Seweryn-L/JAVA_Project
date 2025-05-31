package org.example.config;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ConfigLoader {
    private static final String DEFAULT_CONFIG_PATH = "/config.yaml";

    public static AppConfig loadConfig() {
        return loadConfig(DEFAULT_CONFIG_PATH);
    }

    public static AppConfig loadConfig(String configPath) {
        try {
            // Tworzymy LoaderOptions - wymagane w SnakeYAML 2.x
            LoaderOptions options = new LoaderOptions();

            // Tworzymy Constructor z klasą docelową
            Constructor constructor = new Constructor(AppConfig.class, options);

            // Tworzymy instancję Yaml z konstruktorem
            Yaml yaml = new Yaml(constructor);

            InputStream inputStream;

            // Próbuj załadować z systemu plików
            try {
                inputStream = new FileInputStream(configPath);
            } catch (FileNotFoundException e) {
                // Jeśli nie znaleziono, załaduj z zasobów
                inputStream = ConfigLoader.class.getResourceAsStream(configPath);
                if (inputStream == null) {
                    throw new RuntimeException("Nie można znaleźć pliku konfiguracyjnego: " + configPath);
                }
            }

            // Ładujemy konfigurację
            return yaml.load(inputStream);
        } catch (Exception e) {
            System.err.println("Błąd podczas ładowania konfiguracji: " + e.getMessage());
            e.printStackTrace();
            return getDefaultConfig(); // Zwróć domyślną konfigurację w przypadku błędu
        }
    }


    private static AppConfig getDefaultConfig() {
        // Tworzenie domyślnej konfiguracji
        AppConfig config = new AppConfig();
        config.setBeltCountMax(15);
        config.setBeltWeightMax(29);
        config.setTruckCapacity(73);

        AppConfig.WorkerConfig w1 = new AppConfig.WorkerConfig();
        w1.setBrickMass(1);
        w1.setIntervalMin(500);
        w1.setIntervalMax(1500);
        config.setWorker1(w1);

        AppConfig.WorkerConfig w2 = new AppConfig.WorkerConfig();
        w2.setBrickMass(2);
        w2.setIntervalMin(1500);
        w2.setIntervalMax(2500);
        config.setWorker2(w2);

        AppConfig.WorkerConfig w3 = new AppConfig.WorkerConfig();
        w3.setBrickMass(3);
        w3.setIntervalMin(2500);
        w3.setIntervalMax(3000);
        config.setWorker3(w3);

        return config;
    }
}
