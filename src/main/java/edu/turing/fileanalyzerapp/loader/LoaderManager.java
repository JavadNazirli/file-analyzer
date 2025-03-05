package edu.turing.fileanalyzerapp.loader;

import java.util.ArrayList;
import java.util.List;

public class LoaderManager {
    private final List<FileLoader> loaders;

    public LoaderManager() {
        loaders = new ArrayList<>();
        loaders.add(new CsvLoader());
        loaders.add(new XmlLoader());
        loaders.add(new TxtLoader());
    }

    public FileLoader getLoader(String fileExtension) {
        return loaders.stream()
                .filter(loader -> loader.supports(fileExtension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No loader found for extension: " + fileExtension));
    }
}