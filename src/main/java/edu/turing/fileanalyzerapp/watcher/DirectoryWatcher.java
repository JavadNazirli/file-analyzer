package edu.turing.fileanalyzerapp.watcher;
import edu.turing.fileanalyzerapp.loader.FileLoader;
import edu.turing.fileanalyzerapp.loader.FileLoaderFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DirectoryWatcher {
    private final String directoryPath;
    private final FileLoaderFactory loaderFactory;
    private final ExecutorService executorService;

    public DirectoryWatcher(String directoryPath, FileLoaderFactory loaderFactory) {
        this.directoryPath = directoryPath;
        this.loaderFactory = loaderFactory;
        this.executorService = Executors.newFixedThreadPool(4); // Create 4 thread.
    }

    public void startWatching() {
        File folder = new File(directoryPath);
        while (true) {  // Always check the folder
            File[] files = folder.listFiles(); // Take the files from the folder

            if (files != null) {
                for (File file : files) {
                    executorService.submit(() -> processFile(file)); // Run the tasks in parallel.
                }
            }

            try {
                Thread.sleep(5000); // Check the folder every 5 seconds.
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break; // If the thread is interrupted, stop the loop.
            }
        }
    }

    private void processFile(File file) {
        String extension = getFileExtension(file);
        try {
            FileLoader loader = loaderFactory.createLoader(extension);
            loader.loadFile(file);
        } catch (IllegalArgumentException e) {
            System.out.println("Unsupported file type: " + file.getName());
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndex = name.lastIndexOf(".");
        return (lastIndex == -1) ? "" : name.substring(lastIndex + 1).toLowerCase();
    }
}

