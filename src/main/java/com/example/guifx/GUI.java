package com.example.guifx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
*GUI is the class that controls the JavaFX GUI
*/

public class GUI {
    private VehicleController vehicleController;

    private int id = 0;

    /**
     * Sets the VehicleController in order to insert Cars and change the view of the Sumo-GUI
     * @param vehicleController
    */
    
    public void setVehiclesManager(VehicleController vehicleController){
        this.vehicleController = vehicleController;
    }

    /**
    *Inserts a car and sets the view on it, when Button is clicked
    */
    @FXML
    public void InsertCar(ActionEvent e) {
        System.out.println("Button clicked!");
        id += 1;
        String idf = "id" + id;
        byte lane1 = 0;
        
        /**
        *@throws Exception
        */
        
        try {
            VehicleState car = new VehicleState(idf,"car","route1",lane1);
            vehicleController.injectVehicle(car);
            vehicleController.trackVehicle("View #0",car.getId());
            System.out.println("add a car: " + car.getId());
            /*conn.do_job_set(Vehicle.add(idf, "car", "route1", 0, 0.0, 1.0, lane1)); //Fügt Lastwagen hinzu. ACHTUNG: VehicleType "ev"/"tr" muss in dem .rou.xml File definiert werden.
            conn.do_job_set(Gui.trackVehicle("View #0", idf));*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    @FXML
    public void getCarSpeed(ActionEvent e){
        System.out.println("Button clicked!");
        String idf = "id" + id;
        try {
            if (vehicleController.getIds().contains(idf)){
                System.out.println("Current Speed of vehicle " + idf + " is: " + vehicleController.getVehicleSpeed(idf));
            }
            else {
                System.out.println("Error: No car inserted yet / inserted car left the simulation!");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

}


