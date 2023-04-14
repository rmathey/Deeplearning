module com.example.deepleaningclient {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.databind;

    opens com.example.deepleaningclient to javafx.fxml;
    exports com.example.deepleaningclient;
}