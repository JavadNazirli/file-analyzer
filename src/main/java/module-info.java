module edu.turing.fileanalyzerapp {

    requires javafx.controls;
    requires javafx.fxml;
    requires java.xml;
    requires javafx.graphics;


    exports edu.turing.fileanalyzerapp.gui to javafx.graphics;
    exports edu.turing.fileanalyzerapp.model;
    exports edu.turing.fileanalyzerapp.loader;
    exports edu.turing.fileanalyzerapp.processor;
    exports edu.turing.fileanalyzerapp.watcher;
    exports edu.turing.fileanalyzerapp.config;


    exports edu.turing.fileanalyzerapp to javafx.graphics;
}