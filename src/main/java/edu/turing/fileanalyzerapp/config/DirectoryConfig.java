package edu.turing.fileanalyzerapp.config;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class DirectoryConfig {
    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getInputDirectory() {
        return properties.getProperty("input.directory", "input_directory");
    }

    public static long getMonitoringIntervalSeconds() {
        return Long.parseLong(properties.getProperty("monitoring.interval.seconds", "5"));
    }

    public static void setMonitoringIntervalSeconds(long interval) {
        properties.setProperty("monitoring.interval.seconds", String.valueOf(interval));
        try (FileOutputStream fos = new FileOutputStream("config.properties")) {
            properties.store(fos, "Updated monitoring interval");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}