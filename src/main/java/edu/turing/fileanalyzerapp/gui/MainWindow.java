package edu.turing.fileanalyzerapp.gui;

import edu.turing.fileanalyzerapp.config.DirectoryConfig;
import edu.turing.fileanalyzerapp.loader.LoaderManager;
import edu.turing.fileanalyzerapp.model.TradeRecord;
import edu.turing.fileanalyzerapp.processor.DataProcessor;
import edu.turing.fileanalyzerapp.watcher.DirectoryWatcher;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainWindow {
    private DirectoryWatcher directoryWatcher;
    private DataProcessor dataProcessor;
    private ListView<String> statusListView;
    private TableView<TradeRecord> table;
    private ObservableList<String> statusMessages;
    private ObservableList<TradeRecord> tableData;

    public MainWindow() throws Exception {

        LoaderManager loaderManager = new LoaderManager();
        dataProcessor = new DataProcessor(loaderManager, this);


        directoryWatcher = new DirectoryWatcher(dataProcessor);
        directoryWatcher.startMonitoring();
    }

    public Scene createScene() {

        statusMessages = FXCollections.observableArrayList();


        tableData = FXCollections.observableArrayList();


        statusListView = new ListView<>(statusMessages);
        statusListView.setPrefHeight(150);


        statusListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    getStyleClass().removeAll("error", "processed", "added"); // Önceki sınıfları kaldır
                } else {
                    setText(item);
                    getStyleClass().removeAll("error", "processed", "added"); // Önceki sınıfları kaldır
                    if (item.contains("Error")) {
                        getStyleClass().add("error");
                    } else if (item.contains("Processed")) {
                        getStyleClass().add("processed");
                    } else if (item.contains("Added")) {
                        getStyleClass().add("added");
                    }
                }
            }
        });


        table = new TableView<>();
        table.setItems(tableData);


        TableColumn<TradeRecord, String> noCol = new TableColumn<>("No");
        noCol.setCellValueFactory(cellData -> {
            int index = table.getItems().indexOf(cellData.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });
        noCol.setSortable(false);

        TableColumn<TradeRecord, LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getDate()));
        dateCol.setSortType(TableColumn.SortType.ASCENDING);


        TableColumn<TradeRecord, Number> openCol = new TableColumn<>("Open");
        openCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getOpen()));
        openCol.setSortType(TableColumn.SortType.ASCENDING);


        TableColumn<TradeRecord, Number> highCol = new TableColumn<>("High");
        highCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getHigh()));
        highCol.setSortType(TableColumn.SortType.ASCENDING);


        TableColumn<TradeRecord, Number> lowCol = new TableColumn<>("Low");
        lowCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getLow()));
        lowCol.setSortType(TableColumn.SortType.ASCENDING);


        TableColumn<TradeRecord, Number> closeCol = new TableColumn<>("Close");
        closeCol.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getClose()));
        closeCol.setSortType(TableColumn.SortType.ASCENDING);


        TableColumn<TradeRecord, Number> volumeCol = new TableColumn<>("Volume");
        volumeCol.setCellValueFactory(cellData -> new SimpleLongProperty(cellData.getValue().getVolume()));
        volumeCol.setSortType(TableColumn.SortType.ASCENDING);


        table.getColumns().addAll(noCol, dateCol, openCol, highCol, lowCol, closeCol, volumeCol);


        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        noCol.setMaxWidth(1f * Integer.MAX_VALUE * 10);   // %10
        dateCol.setMaxWidth(1f * Integer.MAX_VALUE * 20); // %20
        openCol.setMaxWidth(1f * Integer.MAX_VALUE * 14); // %14
        highCol.setMaxWidth(1f * Integer.MAX_VALUE * 14); // %14
        lowCol.setMaxWidth(1f * Integer.MAX_VALUE * 14);  // %14
        closeCol.setMaxWidth(1f * Integer.MAX_VALUE * 14); // %14
        volumeCol.setMaxWidth(1f * Integer.MAX_VALUE * 14); // %14


        table.setSortPolicy(param -> true);


        table.getSortOrder().add(dateCol);


        Button clearButton = new Button("Clear Table");
        clearButton.setOnAction(event -> {
            tableData.clear();
            updateStatus("Table cleared. Total records: 0");
        });


        HBox buttonBox = new HBox(10, clearButton);
        buttonBox.setStyle("-fx-padding: 10px; -fx-alignment: center;");


        BorderPane root = new BorderPane();
        root.setTop(statusListView);
        root.setCenter(table);
        root.setBottom(buttonBox);


        Scene scene = new Scene(root, 800, 600);
        String cssPath = "/styles.css";
        java.net.URL cssUrl = getClass().getResource(cssPath);
        if (cssUrl == null) {
            System.err.println("CSS file not found: " + cssPath);
            updateStatus("Error: CSS file not found: " + cssPath);
        } else {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        }


        updateStatus("Application started. Monitoring directory: " + DirectoryConfig.getInputDirectory());

        return scene;
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

    public void shutdown() {
        if (dataProcessor != null) {
            dataProcessor.shutdown();
        }
    }
}