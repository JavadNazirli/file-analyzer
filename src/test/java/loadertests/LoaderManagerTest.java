package loadertests;

import edu.turing.fileanalyzerapp.loader.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoaderManagerTest {

    private LoaderManager loaderManager;

    @BeforeEach
    void setUp() {
        loaderManager = new LoaderManager();
    }

    @Test
    void testGetLoaderForCsv() {
        FileLoader loader = loaderManager.getLoader("csv");
        assertTrue(loader instanceof CsvLoader, "For CSV extension, it should return CsvLoader");
        assertTrue(loader.supports("csv"), "Loader should support csv extension");
    }

    @Test
    void testGetLoaderForXml() {
        FileLoader loader = loaderManager.getLoader("xml");
        assertTrue(loader instanceof XmlLoader, "For XML extension, it should return XmlLoader");
        assertTrue(loader.supports("xml"), "Loader should support xml extension");
    }

    @Test
    void testGetLoaderForTxt() {
        FileLoader loader = loaderManager.getLoader("txt");
        assertTrue(loader instanceof TxtLoader, "For TXT extension, it should return TxtLoader");
        assertTrue(loader.supports("txt"), "Loader should support txt extension");
    }

    @Test
    void testGetLoaderForUpperCaseExtension() {
        FileLoader loader = loaderManager.getLoader("CSV");
        assertTrue(loader instanceof CsvLoader, "For uppercase CSV extension, it should return CsvLoader");
        assertTrue(loader.supports("CSV"), "Loader should support uppercase CSV extension");
    }

    @Test
    void testGetLoaderForUnsupportedExtension() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loaderManager.getLoader("json");
        });
        assertEquals("No loader found for extension: json", exception.getMessage(), "For an unsupported extension, it should throw the correct error message");
    }

    @Test
    void testGetLoaderForEmptyExtension() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loaderManager.getLoader("");
        });
        assertEquals("No loader found for extension: ", exception.getMessage(), "For an empty extension, it should throw the correct error message");
    }

    @Test
    void testGetLoaderForNullExtension() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            loaderManager.getLoader(null);
        });
        assertEquals("No loader found for extension: null", exception.getMessage(), "For a null extension, it should throw the correct error message");
    }
}