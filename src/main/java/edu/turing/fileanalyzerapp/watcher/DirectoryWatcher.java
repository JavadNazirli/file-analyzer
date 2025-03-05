package edu.turing.fileanalyzerapp.watcher;

import edu.turing.fileanalyzerapp.config.DirectoryConfig;
import edu.turing.fileanalyzerapp.processor.DataProcessor;

import java.nio.file.*;
import java.util.HashSet;
import java.util.Set;

public class DirectoryWatcher {
    private final Path directory;
    private final WatchService watchService;
    private final DataProcessor dataProcessor;
    private final Set<String> processedFiles;

    public DirectoryWatcher(DataProcessor dataProcessor) throws Exception {
        this.directory = Paths.get(DirectoryConfig.getInputDirectory());
        this.watchService = FileSystems.getDefault().newWatchService();
        this.dataProcessor = dataProcessor;
        this.processedFiles = new HashSet<>();
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
        System.out.println("Starting directory monitoring...");
        new Thread(() -> {
            while (true) {
                try {
                    WatchKey key = watchService.take();
                    System.out.println("Watch event detected.");
                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            Path filePath = directory.resolve((Path) event.context());
                            System.out.println("New file detected: " + filePath);
                            dataProcessor.processFileAsync(filePath);
                        } else {
                            System.out.println("Event type: " + kind);
                        }
                    }
                    key.reset();
                } catch (Exception e) {
                    System.err.println("Error in DirectoryWatcher: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }).start();
    }
}