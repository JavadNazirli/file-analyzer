package loadertests;

import edu.turing.fileanalyzerapp.loader.CsvLoader;
import edu.turing.fileanalyzerapp.model.TradeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvLoaderTest {

    private CsvLoader csvLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        csvLoader = new CsvLoader();
    }

    @Test
    void testLoadValidCsv() throws Exception {

        Path csvFile = tempDir.resolve("test.csv");
        String csvContent = """
            2023-01-01, 100.5, 105.0, 98.0, 102.3, 1000
            2023-01-02, 102.3, 107.0, 101.0, 106.5, 1500
            """;
        Files.writeString(csvFile, csvContent);


        List<TradeRecord> trades = csvLoader.load(csvFile);


        assertEquals(2, trades.size());

        TradeRecord firstTrade = trades.get(0);
        assertEquals(LocalDate.of(2023, 1, 1), firstTrade.getDate());
        assertEquals(100.5, firstTrade.getOpen(), 0.001);
        assertEquals(105.0, firstTrade.getHigh(), 0.001);
        assertEquals(98.0, firstTrade.getLow(), 0.001);
        assertEquals(102.3, firstTrade.getClose(), 0.001);
        assertEquals(1000L, firstTrade.getVolume());
    }

    @Test
    void testLoadCsvWithInvalidLines() throws Exception {

        Path csvFile = tempDir.resolve("test_invalid.csv");
        String csvContent = """
            2023-01-01, 100.5, 105.0, 98.0, 102.3, 1000
            invalid_line_here
            2023-01-02, 102.3, 107.0, 101.0, 106.5, 1500
            """;
        Files.writeString(csvFile, csvContent);


        List<TradeRecord> trades = csvLoader.load(csvFile);


        assertEquals(2, trades.size());
    }

    @Test
    void testLoadNonExistentFile() {

        Path nonExistentFile = tempDir.resolve("non_existent.csv");


        Exception exception = assertThrows(IOException.class, () -> csvLoader.load(nonExistentFile));
        assertTrue(exception.getMessage().contains("non_existent.csv"));
    }

    @Test
    void testSupportsCsvExtension() {

        assertTrue(csvLoader.supports("csv"));
        assertTrue(csvLoader.supports("CSV"));
        assertFalse(csvLoader.supports("txt"));
        assertFalse(csvLoader.supports("json"));
    }

    @Test
    void testLoadCsvWithMalformedData() throws Exception {

        Path csvFile = tempDir.resolve("test_malformed.csv");
        String csvContent = """
            2023-01-01, 100.5, 105.0, 98.0, 102.3, 1000
            2023-01-02, not_a_number, 107.0, 101.0, 106.5, 1500
            """;
        Files.writeString(csvFile, csvContent);

        assertThrows(NumberFormatException.class, () -> csvLoader.load(csvFile));
    }
}