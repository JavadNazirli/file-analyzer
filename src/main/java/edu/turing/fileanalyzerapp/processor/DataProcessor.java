package edu.turing.fileanalyzerapp.processor;

import edu.turing.fileanalyzerapp.gui.MainWindow;
import edu.turing.fileanalyzerapp.loader.FileLoader;
import edu.turing.fileanalyzerapp.loader.LoaderManager;
import edu.turing.fileanalyzerapp.model.TradeRecord;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataProcessor {
    private final ExecutorService executorService;
    private final LoaderManager loaderManager;
    private final MainWindow mainWindow;

    public DataProcessor(LoaderManager loaderManager, MainWindow mainWindow) {
        this.loaderManager = loaderManager;
        this.mainWindow = mainWindow;
        this.executorService = Executors.newFixedThreadPool(10);
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public void processFileAsync(Path filePath) {
        mainWindow.updateStatus("Processing file: " + filePath);
        String extension = getFileExtension(filePath);
        try {
            System.out.println("Getting loader for extension: " + extension);
            FileLoader loader = loaderManager.getLoader(extension);
            System.out.println("Loader found: " + loader.getClass().getSimpleName());

            CompletableFuture.supplyAsync(() -> {
                        try {
                            System.out.println("Loading file: " + filePath);
                            List<TradeRecord> trades = loader.load(filePath);
                            System.out.println("Loaded " + trades.size() + " records from " + filePath);
                            return trades;
                        } catch (Exception e) {
                            System.err.println("Error in loader: " + e.getMessage());
                            e.printStackTrace();
                            throw new RuntimeException("Failed to load file: " + filePath, e);
                        }
                    }, executorService)
                    .thenAccept(trades -> {
                        System.out.println("Entering thenAccept block for file: " + filePath);
                        if (trades != null && !trades.isEmpty()) {
                            mainWindow.updateStatus("Processed file: " + filePath + " with " + trades.size() + " records.");
                            mainWindow.updateTable(trades);
                        } else if (trades != null) {
                            mainWindow.updateStatus("Processed file: " + filePath + " but no records were found.");
                        } else {
                            mainWindow.updateStatus("Failed to process file: " + filePath);
                        }
                    })
                    .exceptionally(throwable -> {
                        System.err.println("Error in CompletableFuture: " + throwable.getMessage());
                        throwable.printStackTrace();
                        mainWindow.updateStatus("Failed to process file: " + filePath + " - " + throwable.getMessage());
                        return null;
                    });
        } catch (IllegalArgumentException e) {
            mainWindow.updateStatus("Unsupported file type: " + filePath + " - " + e.getMessage());
        }
    }

    private String getFileExtension(Path filePath) {
        String fileName = filePath.getFileName().toString();
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}