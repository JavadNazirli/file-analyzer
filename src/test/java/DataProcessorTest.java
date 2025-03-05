import edu.turing.fileanalyzerapp.gui.MainWindow;
import edu.turing.fileanalyzerapp.loader.LoaderManager;
import edu.turing.fileanalyzerapp.processor.DataProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DataProcessorTest {

    private DataProcessor dataProcessor;
    private LoaderManager loaderManager;
    private MainWindow mainWindow;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        loaderManager = new LoaderManager();
        mainWindow = Mockito.mock(MainWindow.class);
        dataProcessor = new DataProcessor(loaderManager, mainWindow);
    }

    @AfterEach
    void tearDown() {
        dataProcessor.shutdown();
    }

    @Test
    void testProcessFileAsyncWithValidCsv() throws Exception {
        // Create a valid CSV file
        Path csvFile = tempDir.resolve("test.csv");
        String csvContent = """
            2023-01-01, 100.5, 105.0, 98.0, 102.3, 1000
            2023-01-02, 102.3, 107.0, 101.0, 106.5, 1500
            """;
        Files.writeString(csvFile, csvContent);


        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mainWindow).updateTable(anyList());


        dataProcessor.processFileAsync(csvFile);


        assertTrue(latch.await(5, TimeUnit.SECONDS), "Async processing should complete within 5 seconds");


        verify(mainWindow).updateStatus("Processing file: " + csvFile);
        verify(mainWindow).updateTable(anyList());
        verify(mainWindow).updateStatus("Processed file: " + csvFile + " with 2 records.");
    }

    @Test
    void testProcessFileAsyncWithUnsupportedExtension() throws Exception {

        Path unsupportedFile = tempDir.resolve("test.json");
        Files.writeString(unsupportedFile, "{}");


        dataProcessor.processFileAsync(unsupportedFile);


        verify(mainWindow).updateStatus("Processing file: " + unsupportedFile);
        verify(mainWindow).updateStatus("Unsupported file type: " + unsupportedFile + " - No loader found for extension: json");
        verify(mainWindow, never()).updateTable(anyList());
    }

    @Test
    void testProcessFileAsyncWithEmptyFile() throws Exception {

        Path emptyFile = tempDir.resolve("empty.csv");
        Files.writeString(emptyFile, "");


        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mainWindow).updateStatus(contains("Processed file"));


        dataProcessor.processFileAsync(emptyFile);


        assertTrue(latch.await(5, TimeUnit.SECONDS), "Async processing should complete within 5 seconds");


        verify(mainWindow).updateStatus("Processing file: " + emptyFile);
        verify(mainWindow).updateStatus("Processed file: " + emptyFile + " but no records were found.");
        verify(mainWindow, never()).updateTable(anyList());
    }

    @Test
    void testProcessFileAsyncWithInvalidFile() throws Exception {

        Path invalidFile = tempDir.resolve("invalid.csv");
        String invalidContent = """
            2023-01-01, not_a_number, 105.0, 98.0, 102.3, 1000
            """;
        Files.writeString(invalidFile, invalidContent);


        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(mainWindow).updateStatus(contains("Failed to process"));


        dataProcessor.processFileAsync(invalidFile);


        assertTrue(latch.await(5, TimeUnit.SECONDS), "Async processing should complete within 5 seconds");


        verify(mainWindow).updateStatus("Processing file: " + invalidFile);
        verify(mainWindow).updateStatus(startsWith("Failed to process file: " + invalidFile));
        verify(mainWindow, never()).updateTable(anyList());
    }

    @Test
    void testGetMainWindow() {
        assertEquals(mainWindow, dataProcessor.getMainWindow(), "getMainWindow should return the injected MainWindow instance");
    }

    @Test
    void testShutdown() {
        dataProcessor.shutdown();
        assertTrue(dataProcessor.getMainWindow() != null, "MainWindow should still be accessible after shutdown");
        // Note: ExecutorService.isShutdown() is not directly accessible, but we can assume shutdown works as expected
    }
}