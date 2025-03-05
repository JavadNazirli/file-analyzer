package loadertests;

import edu.turing.fileanalyzerapp.loader.XmlLoader;
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

class XmlLoaderTest {

    private XmlLoader xmlLoader;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        xmlLoader = new XmlLoader();
    }

    @Test
    void testLoadValidXml() throws Exception {

        Path xmlFile = tempDir.resolve("test.xml");
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <trades>
                <value date="2023-01-01" open="100.5" high="105.0" low="98.0" close="102.3" volume="1000"/>
                <value date="2023-01-02" open="102.3" high="107.0" low="101.0" close="106.5" volume="1500"/>
            </trades>
            """;
        Files.writeString(xmlFile, xmlContent);


        List<TradeRecord> trades = xmlLoader.load(xmlFile);


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
    void testLoadXmlWithMissingAttributes() throws Exception {

        Path xmlFile = tempDir.resolve("test_missing.xml");
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <trades>
                <value date="2023-01-01" open="100.5" high="105.0" low="98.0" close="102.3" volume="1000"/>
                <value date="2023-01-02" open="102.3" high="107.0"/>
            </trades>
            """;
        Files.writeString(xmlFile, xmlContent);


        assertThrows(NumberFormatException.class, () -> xmlLoader.load(xmlFile));
    }

    @Test
    void testLoadNonExistentFile() {

        Path nonExistentFile = tempDir.resolve("non_existent.xml");


        Exception exception = assertThrows(IOException.class, () -> xmlLoader.load(nonExistentFile));
        assertTrue(exception.getMessage().contains("non_existent.xml"));
    }

    @Test
    void testSupportsXmlExtension() {

        assertTrue(xmlLoader.supports("xml"));
        assertTrue(xmlLoader.supports("XML"));
        assertFalse(xmlLoader.supports("csv"));
        assertFalse(xmlLoader.supports("txt"));
    }

    @Test
    void testLoadXmlWithMalformedData() throws Exception {

        Path xmlFile = tempDir.resolve("test_malformed.xml");
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <trades>
                <value date="2023-01-01" open="100.5" high="105.0" low="98.0" close="102.3" volume="1000"/>
                <value date="2023-01-02" open="not_a_number" high="107.0" low="101.0" close="106.5" volume="1500"/>
            </trades>
            """;
        Files.writeString(xmlFile, xmlContent);


        assertThrows(NumberFormatException.class, () -> xmlLoader.load(xmlFile));
    }

    @Test
    void testLoadEmptyXml() throws Exception {

        Path xmlFile = tempDir.resolve("test_empty.xml");
        String xmlContent = """
            <?xml version="1.0" encoding="UTF-8"?>
            <trades></trades>
            """;
        Files.writeString(xmlFile, xmlContent);


        List<TradeRecord> trades = xmlLoader.load(xmlFile);


        assertTrue(trades.isEmpty());
    }
}