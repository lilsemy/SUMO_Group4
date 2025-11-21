package com.example.guifx;
import de.tudresden.sumo.cmd.Gui;
import de.tudresden.sumo.cmd.Vehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
//import static com.example.guifx.Connection.conn;

public class GUI {
    // hier sollte aus connection kommen
    private VehiclesMangagement vehiclesManager1;

    private int id = 1;

    public void setVehiclesManager(VehiclesMangagement vehiclesManager){
        this.vehiclesManager1 = vehiclesManager;
    }
    @FXML
    public void InsertCar(ActionEvent e) {
        System.out.println("Button clicked!");
        String idf = "id" + id;
        byte lane1 = 0;
        try {
            myVehicle car = new myVehicle(idf,"car","route1",lane1);
            vehiclesManager1.injectVehicle(car);

            vehiclesManager1.trackVehicle("View #0",car.getId());
            System.out.println("add a car: " + car.getId());

            /*conn.do_job_set(Vehicle.add(idf, "car", "route1", 0, 0.0, 1.0, lane1)); //Fügt Lastwagen hinzu. ACHTUNG: VehicleType "ev"/"tr" muss in dem .rou.xml File definiert werden.
            conn.do_job_set(Gui.trackVehicle("View #0", idf));*/
            id += 1;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


