package edu.turing.fileanalyzerapp.loader;

import edu.turing.fileanalyzerapp.model.TradeRecord;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class XmlLoader implements FileLoader {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public List<TradeRecord> load(Path filePath) throws Exception {
        List<TradeRecord> trades = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(filePath.toFile());
        doc.getDocumentElement().normalize();

        NodeList valueNodes = doc.getElementsByTagName("value");
        for (int i = 0; i < valueNodes.getLength(); i++) {
            Element valueElement = (Element) valueNodes.item(i);
            String dateStr = valueElement.getAttribute("date");
            double open = Double.parseDouble(valueElement.getAttribute("open"));
            double high = Double.parseDouble(valueElement.getAttribute("high"));
            double low = Double.parseDouble(valueElement.getAttribute("low"));
            double close = Double.parseDouble(valueElement.getAttribute("close"));
            long volume = Long.parseLong(valueElement.getAttribute("volume"));

            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            TradeRecord trade = new TradeRecord(date, open, high, low, close, volume);
            trades.add(trade);
        }
        return trades;
    }

    @Override
    public boolean supports(String fileExtension) {
        return "xml".equalsIgnoreCase(fileExtension);
    }
}
