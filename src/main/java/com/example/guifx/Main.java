package com.example.guifx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Optional;

/**
    *Main is the Initializer of the GUI
    *@see class Application
    */

public class Main extends Application {
    private GUI gui;
    private boolean deleteCsv = false;
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

        LOG.info("Setting Simulation Controller for GUI + starting SUMO connection");
        SimulationController simController = new SimulationController();
        gui.setSimulationController(simController);
        simController.makeConnection();

        LOG.info("Application Start successful");


        stage.setOnCloseRequest(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Close Application");
            alert.setHeaderText("The simulation has finished.");
            alert.setContentText("Do you want to keep the CSV file?");

            ButtonType Keep = new ButtonType("Keep");
            ButtonType Delete = new ButtonType("Delete");
            ButtonType Cancel = new ButtonType("Cancel");
            alert.getButtonTypes().setAll(Keep, Delete, Cancel);
            Optional< ButtonType> result  = alert.showAndWait();
            if(result.isPresent()){
                if(result.get() == Delete){
                    deleteCsv = true;
                } else if (result.get() == Cancel) {
                    event.consume();

                }
            }
        });
    }




        @Override
        public void stop() throws Exception {
            if (gui != null) {
                gui.stopAll();
            }
            if (deleteCsv) {
                File file = new File("simulation.csv");
                if(file.exists()){
                    if(file.delete()){
                        System.out.println("CSV file deleted successfully");
                    } else {
                        System.out.println("Failed to delete CSV");
                }
            }
        }
            super.stop();

        }
}



