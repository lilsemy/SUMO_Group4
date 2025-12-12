package com.example.guifx;

import com.example.guifx.controller.SimulationController;
import com.example.guifx.model.Statistics;
import com.example.guifx.controller.GUI;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;


/**
    *Main is the Initializer of the GUI
    *@see class Application
    */

public class Main extends Application {
    @Override

    /**
    *starts scene
    *@throws Exception
    */

    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("GUI-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        stage.setTitle("SumoController");
        stage.setScene(scene);
        stage.show();

        GUI gui = fxmlLoader.getController();

        SimulationController simController = new SimulationController();
        gui.setSimulationController(simController);
        simController.makeConnection();

        Statistics stat = new Statistics(simController);
    }











}
