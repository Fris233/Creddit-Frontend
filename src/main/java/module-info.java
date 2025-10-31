module com.fhm.take2 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;

    opens com.fhm.take2 to javafx.fxml;
    exports com.fhm.take2;
}