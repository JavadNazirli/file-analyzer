package edu.turing.fileanalyzerapp.watcher;

import edu.turing.fileanalyzerapp.config.DirectoryConfig;
import edu.turing.fileanalyzerapp.processor.DataProcessor;

import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DirectoryWatcher {
    private final Path directory;
    private final WatchService watchService;
    private final DataProcessor dataProcessor;
    private final long monitoringIntervalSeconds;
    private final Set<String> processedFiles;
    private volatile boolean isRunning; // İzleme döngüsünü kontrol etmek için

    public DirectoryWatcher(DataProcessor dataProcessor) throws Exception {
        this(dataProcessor, DirectoryConfig.getMonitoringIntervalSeconds());
    }

    public DirectoryWatcher(DataProcessor dataProcessor, long monitoringIntervalSeconds) throws Exception {
        this.directory = Paths.get(DirectoryConfig.getInputDirectory());
        this.watchService = FileSystems.getDefault().newWatchService();
        this.dataProcessor = dataProcessor;
        this.monitoringIntervalSeconds = monitoringIntervalSeconds;
        this.processedFiles = new HashSet<>();
        this.isRunning = true;
        System.out.println("Watching directory: " + directory.toAbsolutePath());
        directory.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE);

        // Başlangıçta mevcut dosyaları tara ve işle
        scanExistingFiles();
    }

    private void scanExistingFiles() {
        try {
            Files.walk(directory, 1)
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        System.out.println("Found existing file: " + filePath);
                        dataProcessor.processFileAsync(filePath);
                        String fileName = filePath.getFileName().toString();
                        processedFiles.add(fileName);
                    });
        } catch (Exception e) {
            System.err.println("Error scanning existing files: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void startMonitoring() {
        System.out.println("Starting directory monitoring with interval: " + monitoringIntervalSeconds + " seconds...");
        new Thread(() -> {
            while (isRunning) {
                try {
                    WatchKey key = watchService.take();
                    System.out.println("Watch event detected.");
                    String deletedFileName = null;

                    // DELETE olaylarını kontrol et
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            Path deletedPath = (Path) event.context();
                            deletedFileName = deletedPath.getFileName().toString();
                            System.out.println("File deleted: " + deletedFileName);
                            processedFiles.remove(deletedFileName);
                        }
                    }

                    // CREATE olaylarını kontrol et
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path filePath = directory.resolve((Path) event.context());
                            String fileName = filePath.getFileName().toString();

                            // Eğer bu bir yeniden adlandırma işlemiyse (önce DELETE sonra CREATE olmuşsa)
                            if (deletedFileName != null && processedFiles.contains(deletedFileName)) {
                                System.out.println("Detected rename: " + deletedFileName + " to " + fileName);
                                dataProcessor.getMainWindow().updateStatus("File renamed: " + deletedFileName + " to " + fileName);
                                processedFiles.add(fileName);
                                continue; // Yeniden adlandırılan dosyayı tekrar işleme
                            }

                            // Yeni dosya ise işle
                            if (!processedFiles.contains(fileName)) {
                                System.out.println("New file detected: " + filePath);
                                dataProcessor.processFileAsync(filePath);
                                processedFiles.add(fileName);
                            } else {
                                System.out.println("File already processed: " + filePath);
                            }
                        } else {
                            System.out.println("Event type: " + kind);
                        }
                    }
                    key.reset();
                    TimeUnit.SECONDS.sleep(monitoringIntervalSeconds);
                } catch (Exception e) {
                    System.err.println("Error in DirectoryWatcher: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void stopMonitoring() {
        isRunning = false;
        try {
            watchService.close();
        } catch (Exception e) {
            System.err.println("Error closing WatchService: " + e.getMessage());
        }
    }
}