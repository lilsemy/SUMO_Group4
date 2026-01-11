module com.example.guifx {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;

    requires java.desktop;
    requires libtraci;
    requires java.logging;

    opens com.example.guifx to javafx.fxml;
    exports com.example.guifx;
}