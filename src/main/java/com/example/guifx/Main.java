package com.example.guifx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
    *Main is the Initializer of the GUI
    *@see class Application
    */

public class Main extends Application {
    private GUI gui;
    private static final Logger LOG = LogManager.getLogger(Main.class.getName());
    @Override

    /**
    *starts scene
    *@throws Exception
    */

    public void start(Stage stage) throws Exception {
        LOG.info("Starting Application");
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.getIcons().add(
                new Image(Main.class.getResourceAsStream("/com/example/guifx/AppIcon.png"))
        );

        stage.setTitle("SumoController");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();

        LOG.info("Initializing GUI");
        GUI gui = fxmlLoader.getController();
        this.gui = gui;

        LOG.info("Setting Simulation Controller for GUI");
        SimulationController simController = new SimulationController();
        gui.setSimulationController(simController);

        LOG.info("Application Start successful");
    }
        @Override
        public void stop() throws Exception {
        if (gui != null) {
           gui.stopAll();
        }
            super.stop();

        }
}



