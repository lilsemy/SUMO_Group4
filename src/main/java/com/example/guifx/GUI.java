package com.example.guifx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
*GUI is the class that controls the JavaFX GUI
*/

public class GUI {
    private SimulationController simController;

    public GUI() throws Exception {
    }

    /**
     * Sets the SimulationController in order to insert Cars and change the view of the Sumo-GUI
     * @param simulationController
    */
    
    public void setSimulationController(SimulationController simulationController){
        this.simController = simulationController;
    }

    /**
    *Inserts a car and sets the view on it, when Button is clicked
    */
    //Moved it to the simulation controller
    @FXML
    public void commandSpawnVehicle(ActionEvent e) {
        System.out.println("Spawning new vehicle!");
        simController.spawnVehicle();
    }
    //Moved it to the simulation controller
    @FXML
    public void commandGetVehicleSpeed(ActionEvent e){
        System.out.println("Fetching the speed of vehicle!");
        simController.getVehicleSpeed();
    }

    public void commandChangePhase(ActionEvent e){
        System.out.println("Changing TrafficLight Phase!");
        simController.changePhase();
    }

}


