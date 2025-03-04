module edu.turing.fileanalyzerapp {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.dlsc.formsfx;

    opens edu.turing.fileanalyzerapp to javafx.fxml;
    exports edu.turing.fileanalyzerapp;
}