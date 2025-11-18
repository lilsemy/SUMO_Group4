package com.example.guifx;
import de.tudresden.sumo.cmd.Gui;
import de.tudresden.sumo.cmd.Vehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import static com.example.guifx.Connection.conn;

public class GUI {
    @FXML
    public void InsertCar(ActionEvent e) {
        System.out.println("Button clicked!");

        int id = 1;
        String idf = "id" + id;
        byte lane1 = 0;
        try {
            conn.do_job_set(Vehicle.add(idf, "car", "route1", 0, 0.0, 1.0, lane1)); //Fügt Lastwagen hinzu. ACHTUNG: VehicleType "ev"/"tr" muss in dem .rou.xml File definiert werden.
            conn.do_job_set(Gui.trackVehicle("View #0", idf));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}


