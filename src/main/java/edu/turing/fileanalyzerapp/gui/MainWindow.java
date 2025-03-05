package edu.turing.fileanalyzerapp.gui;

import edu.turing.fileanalyzerapp.config.DirectoryConfig;
import edu.turing.fileanalyzerapp.loader.LoaderManager;
import edu.turing.fileanalyzerapp.model.TradeRecord;
import edu.turing.fileanalyzerapp.processor.DataProcessor;
import edu.turing.fileanalyzerapp.watcher.DirectoryWatcher;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainWindow extends Application {
    private DirectoryWatcher directoryWatcher;
    private DataProcessor dataProcessor;
    private ListView<String> statusListView;
    private TableView<TradeRecord> table;
    private ObservableList<String> statusMessages;
    private ObservableList<TradeRecord> tableData; // Tablo verilerini saklamak için

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Durum mesajları için ObservableList
        statusMessages = FXCollections.observableArrayList();

        // Tablo verileri için ObservableList
        tableData = FXCollections.observableArrayList();

        // Durum alanı (ListView)
        statusListView = new ListView<>(statusMessages);
        statusListView.setPrefHeight(150);

        // Tablo
        table = new TableView<>();
        table.setItems(tableData);

        // "No" sütunu (sıra numarası)
        TableColumn<TradeRecord, String> noCol = new TableColumn<>("No");
        noCol.setCellValueFactory(cellData -> {
            int index = table.getItems().indexOf(cellData.getValue()) + 1; // 1'den başlasın
            return new SimpleStringProperty(String.valueOf(index));
        });

        TableColumn<TradeRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
        TableColumn<TradeRecord, Number> openCol = new TableColumn<>("Open");
        openCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getOpen()));
        TableColumn<TradeRecord, Number> highCol = new TableColumn<>("High");
        highCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getHigh()));
        TableColumn<TradeRecord, Number> lowCol = new TableColumn<>("Low");
        lowCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getLow()));
        TableColumn<TradeRecord, Number> closeCol = new TableColumn<>("Close");
        closeCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getClose()));
        TableColumn<TradeRecord, Number> volumeCol = new TableColumn<>("Volume");
        volumeCol.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getVolume()));
        table.getColumns().addAll(noCol, dateCol, openCol, highCol, lowCol, closeCol, volumeCol);

        // Sütun genişliklerini tablonun tamamını kaplayacak şekilde oransal ayarlama
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Tablonun tamamını kaplar
        noCol.setMaxWidth(1f * Integer.MAX_VALUE * 10);   // %10
        dateCol.setMaxWidth(1f * Integer.MAX_VALUE * 20); // %20
        openCol.setMaxWidth(1f * Integer.MAX_VALUE * 14); // %14
        highCol.setMaxWidth(1f * Integer.MAX_VALUE * 14); // %14
        lowCol.setMaxWidth(1f * Integer.MAX_VALUE * 14);  // %14
        closeCol.setMaxWidth(1f * Integer.MAX_VALUE * 14); // %14
        volumeCol.setMaxWidth(1f * Integer.MAX_VALUE * 14); // %14

        // Tabloyu sıralanabilir yapma
        table.setSortPolicy(param -> true);

        // Clear Table butonu
        Button clearButton = new Button("Clear Table");
        clearButton.setOnAction(event -> {
            tableData.clear();
            updateStatus("Table cleared. Total records: 0");
        });

        // Butonları bir HBox içinde düzenleme
        HBox buttonBox = new HBox(10, clearButton);
        buttonBox.setStyle("-fx-padding: 10px; -fx-alignment: center;");

        // Düzen (BorderPane)
        BorderPane root = new BorderPane();
        root.setTop(statusListView);
        root.setCenter(table);
        root.setBottom(buttonBox);

        // Scene ve CSS
        Scene scene = new Scene(root, 800, 600);
        String cssPath = "/styles.css";
        java.net.URL cssUrl = getClass().getResource(cssPath);
        if (cssUrl == null) {
            System.err.println("CSS file not found: " + cssPath);
            updateStatus("Error: CSS file not found: " + cssPath);
        } else {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }

        primaryStage.setScene(scene);
        primaryStage.setTitle("File Processing App");

        // Tam ekran yapma
        primaryStage.setMaximized(true);

        primaryStage.show();

        // LoaderManager ve DataProcessor başlat
        LoaderManager loaderManager = new LoaderManager();
        dataProcessor = new DataProcessor(loaderManager, this);

        // DirectoryWatcher başlat
        directoryWatcher = new DirectoryWatcher(dataProcessor);
        directoryWatcher.startMonitoring();

        updateStatus("Application started. Monitoring directory: " + DirectoryConfig.getInputDirectory());
    }

    public void updateStatus(String message) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String formattedMessage = timestamp + " - " + message;
        Platform.runLater(() -> statusMessages.add(formattedMessage));
    }

    public void updateTable(List<TradeRecord> trades) {
        Platform.runLater(() -> {
            tableData.addAll(trades);
            updateStatus("Added " + trades.size() + " records. Total records: " + tableData.size());
        });
    }

    @Override
    public void stop() {
        if (dataProcessor != null) {
            dataProcessor.shutdown();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}