package edu.turing.fileanalyzerapp.loader;

import edu.turing.fileanalyzerapp.model.TradeRecord;

import java.nio.file.Path;
import java.util.List;

public interface FileLoader {
    List<TradeRecord> load(Path filePath) throws Exception;

    boolean supports(String fileExtension);
}
