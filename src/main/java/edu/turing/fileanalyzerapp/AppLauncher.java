package edu.turing.fileanalyzerapp;

import edu.turing.fileanalyzerapp.gui.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;

public class AppLauncher extends Application {
    private MainWindow mainWindow;

    @Override
    public void start(Stage primaryStage) throws Exception {
        mainWindow = new MainWindow();


        primaryStage.setScene(mainWindow.createScene());
        primaryStage.setTitle("File Processing App");
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    @Override
    public void stop() {
        if (mainWindow != null) {
            mainWindow.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}