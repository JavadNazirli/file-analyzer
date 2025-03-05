package edu.turing.fileanalyzerapp.loader;

import edu.turing.fileanalyzerapp.model.TradeRecord;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class TxtLoader implements FileLoader {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<TradeRecord> load(Path filePath) throws Exception {
        List<TradeRecord> trades = new ArrayList<>();
        List<String> lines = Files.readAllLines(filePath);

        for (String line : lines) {
            String[] parts = line.split(";");
            if (parts.length == 6) {
                LocalDate date = LocalDate.parse(parts[0].trim(), DATE_FORMATTER);
                double open = Double.parseDouble(parts[1].trim());
                double high = Double.parseDouble(parts[2].trim());
                double low = Double.parseDouble(parts[3].trim());
                double close = Double.parseDouble(parts[4].trim());
                long volume = Long.parseLong(parts[5].trim());

                TradeRecord trade = new TradeRecord(date, open, high, low, close, volume);
                trades.add(trade);
            }
        }
        return trades;
    }

    @Override
    public boolean supports(String fileExtension) {
        return "txt".equalsIgnoreCase(fileExtension);
    }
}