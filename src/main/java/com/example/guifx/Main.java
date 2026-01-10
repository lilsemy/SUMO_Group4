package com.example.guifx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;


/**
    *Main is the Initializer of the GUI
    *@see class Application
    */

public class Main extends Application {
    private GUI gui;
    @Override

    /**
    *starts scene
    *@throws Exception
    */

    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.getIcons().add(
                new Image(Main.class.getResourceAsStream("/com/example/guifx/AppIcon.png"))
        );

        stage.setTitle("SumoController");
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();

        GUI gui = fxmlLoader.getController();
        this.gui = gui;

        SimulationController simController = new SimulationController();
        gui.setSimulationController(simController);
        simController.makeConnection();

    }
        @Override
        public void stop() throws Exception {
        if (gui != null) {
           // gui.stopAll();
        }
            super.stop();

        }
}



