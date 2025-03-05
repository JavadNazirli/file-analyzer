package loadertests;

import edu.turing.fileanalyzerapp.model.TradeRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import edu.turing.fileanalyzerapp.loader.TxtLoader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TxtLoaderTest {

    private TxtLoader txtLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        txtLoader = new TxtLoader();
    }

    @Test
    void testLoadValidTxt() throws Exception {
        Path txtFile = tempDir.resolve("test.txt");
        String txtContent = """
            2023-01-01;100.5;105.0;98.0;102.3;1000
            2023-01-02;102.3;107.0;101.0;106.5;1500
            """;
        Files.writeString(txtFile, txtContent);

        List<TradeRecord> trades = txtLoader.load(txtFile);
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
    void testLoadTxtWithInvalidLines() throws Exception {
        Path txtFile = tempDir.resolve("test_invalid.txt");
        String txtContent = """
            2023-01-01;100.5;105.0;98.0;102.3;1000
            invalid_line_here
            2023-01-02;102.3;107.0;101.0;106.5;1500
            """;
        Files.writeString(txtFile, txtContent);


        List<TradeRecord> trades = txtLoader.load(txtFile);


        assertEquals(2, trades.size());
    }

    @Test
    void testLoadNonExistentFile() {

        Path nonExistentFile = tempDir.resolve("non_existent.txt");


        Exception exception = assertThrows(IOException.class, () -> txtLoader.load(nonExistentFile));
        assertTrue(exception.getMessage().contains("non_existent.txt"));
    }

    @Test
    void testSupportsTxtExtension() {

        assertTrue(txtLoader.supports("txt"));
        assertTrue(txtLoader.supports("TXT"));
        assertFalse(txtLoader.supports("csv"));
        assertFalse(txtLoader.supports("xml"));
    }

    @Test
    void testLoadTxtWithMalformedData() throws Exception {

        Path txtFile = tempDir.resolve("test_malformed.txt");
        String txtContent = """
            2023-01-01;100.5;105.0;98.0;102.3;1000
            2023-01-02;not_a_number;107.0;101.0;106.5;1500
            """;
        Files.writeString(txtFile, txtContent);


        assertThrows(NumberFormatException.class, () -> txtLoader.load(txtFile));
    }

    @Test
    void testLoadEmptyTxt() throws Exception {

        Path txtFile = tempDir.resolve("test_empty.txt");
        String txtContent = "";
        Files.writeString(txtFile, txtContent);


        List<TradeRecord> trades = txtLoader.load(txtFile);


        assertTrue(trades.isEmpty());
    }
}