import edu.turing.fileanalyzerapp.config.DirectoryConfig;
import edu.turing.fileanalyzerapp.processor.DataProcessor;
import edu.turing.fileanalyzerapp.watcher.DirectoryWatcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DirectoryWatcherTest {

    private DirectoryWatcher directoryWatcher;
    private DataProcessor dataProcessor;
    private Properties originalProperties;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        originalProperties = DirectoryConfig.getProperties();


        Properties testProperties = new Properties();
        testProperties.setProperty("input.directory", tempDir.toString());
        DirectoryConfig.setProperties(testProperties);

        dataProcessor = Mockito.mock(DataProcessor.class);
        directoryWatcher = new DirectoryWatcher(dataProcessor);
    }

    @AfterEach
    void tearDown() {

        DirectoryConfig.setProperties(originalProperties);
    }

    @Test
    void testScanExistingFiles() throws Exception {

        Path file1 = tempDir.resolve("test1.csv");
        Path file2 = tempDir.resolve("test2.txt");
        Files.writeString(file1, "2023-01-01,100.5,105.0,98.0,102.3,1000");
        Files.writeString(file2, "2023-01-01;100.5;105.0;98.0;102.3;1000");


        directoryWatcher = new DirectoryWatcher(dataProcessor);


        verify(dataProcessor, times(1)).processFileAsync(file1);
        verify(dataProcessor, times(1)).processFileAsync(file2);


        Set<String> processedFiles = getProcessedFiles();
        assertTrue(processedFiles.contains("test1.csv"), "test1.csv should be in processedFiles");
        assertTrue(processedFiles.contains("test2.txt"), "test2.txt should be in processedFiles");
    }

    @Test
    void testStartMonitoringNewFile() throws Exception {

        directoryWatcher.startMonitoring();


        CountDownLatch latch = new CountDownLatch(1);
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(dataProcessor).processFileAsync(any(Path.class));


        Path newFile = tempDir.resolve("newfile.csv");
        Files.writeString(newFile, "2023-01-01,100.5,105.0,98.0,102.3,1000");


        assertTrue(latch.await(5, TimeUnit.SECONDS), "New file should be processed within 5 seconds");


        verify(dataProcessor, times(1)).processFileAsync(newFile);
    }

    @Test
    void testScanExistingFilesWithNoFiles() throws Exception {

        directoryWatcher = new DirectoryWatcher(dataProcessor);


        verify(dataProcessor, never()).processFileAsync(any(Path.class));


        Set<String> processedFiles = getProcessedFiles();
        assertTrue(processedFiles.isEmpty(), "processedFiles should be empty when no files exist");
    }

    @Test
    void testStartMonitoringDoesNotReprocessExistingFiles() throws Exception {

        Path existingFile = tempDir.resolve("existing.csv");
        Files.writeString(existingFile, "2023-01-01,100.5,105.0,98.0,102.3,1000");


        directoryWatcher = new DirectoryWatcher(dataProcessor);


        directoryWatcher.startMonitoring();


        reset(dataProcessor);


        CountDownLatch latch = new CountDownLatch(1);
        Thread.sleep(1000);
        latch.countDown();


        verify(dataProcessor, never()).processFileAsync(existingFile);
    }


    private Set<String> getProcessedFiles() throws Exception {
        Field processedFilesField = DirectoryWatcher.class.getDeclaredField("processedFiles");
        processedFilesField.setAccessible(true);
        return (Set<String>) processedFilesField.get(directoryWatcher);
    }
}