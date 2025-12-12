module com.example.guifx {
    requires javafx.controls;
    requires javafx.fxml;
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

    opens com.example.guifx to javafx.fxml;
    opens com.example.guifx.model to javafx.fxml;
    opens com.example.guifx.view to javafx.fxml;
    opens com.example.guifx.controller to javafx.fxml;
    opens com.example.guifx.service to javafx.fxml;
    opens com.example.guifx.util to javafx.fxml;
    opens com.example.guifx.config to javafx.fxml;
    exports com.example.guifx;
    exports com.example.guifx.model;
    exports com.example.guifx.view;
    exports com.example.guifx.controller;
    exports com.example.guifx.service;
    exports com.example.guifx.util;
    exports com.example.guifx.config;
}