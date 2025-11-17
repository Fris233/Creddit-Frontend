module com.fhm.take2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.google.gson;
    requires gson.extras;
    requires java.sql;

    opens com.fhm.take2 to javafx.fxml;
    opens com.crdt to com.google.gson;
    exports com.fhm.take2;
}