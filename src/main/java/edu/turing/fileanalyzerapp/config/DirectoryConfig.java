package edu.turing.fileanalyzerapp.config;

import java.io.FileInputStream;
import java.util.Properties;

public class DirectoryConfig {
    private static Properties properties = new Properties();

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

    public static Properties getProperties() {
        return properties;
    }

    public static void setProperties(Properties newProperties) {
        properties = newProperties;
    }
}