package edu.turing.fileanalyzerapp.processor;
import edu.turing.fileanalyzerapp.gui.MainWindow;
import edu.turing.fileanalyzerapp.loader.FileLoader ;
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

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public DataProcessor(LoaderManager loaderManager, MainWindow mainWindow) {
        this.loaderManager = loaderManager;
        this.mainWindow = mainWindow;
        this.executorService = Executors.newFixedThreadPool(10); // Paralel işlem için thread pool
    }

    public void processFileAsync(Path filePath) {
        String extension = getFileExtension(filePath);
        FileLoader loader = loaderManager.getLoader(extension);

        CompletableFuture.supplyAsync(() -> {
            try {
                return loader.load(filePath);
            } catch (Exception e) {
                mainWindow.updateStatus("Error processing file: " + filePath + " - " + e.getMessage());
                return null;
            }
        }, executorService).thenAccept(trades -> {
            if (trades != null) {
                mainWindow.updateStatus("Processed file: " + filePath + " with " + trades.size() + " records.");
                mainWindow.updateTable(trades);
            }
        });
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