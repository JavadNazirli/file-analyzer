module edu.turing.fileanalyzerapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens edu.turing.fileanalyzerapp to javafx.fxml;
    exports edu.turing.fileanalyzerapp;
    exports edu.turing.fileanalyzerapp.watcher;
    opens edu.turing.fileanalyzerapp.watcher to javafx.fxml;
    exports edu.turing.fileanalyzerapp.loader;
    opens edu.turing.fileanalyzerapp.loader to javafx.fxml;
    exports edu.turing.fileanalyzerapp.processor;
    opens edu.turing.fileanalyzerapp.processor to javafx.fxml;
    exports edu.turing.fileanalyzerapp.model;
    opens edu.turing.fileanalyzerapp.model to javafx.fxml;
}