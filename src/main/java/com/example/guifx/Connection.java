package com.example.guifx;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;


//TODO: for-each Schleife?

/**
* Connection is a
*/

public class Connection {
    private TraciConnect simulation;
    private VehiclesMangagement vehiclesManager;

    private GUI controller1;

    /**
    *
    *@throws
    */
    
    public Connection() throws Exception {
        simulation =new TraciConnect();
        simulation.connect();
        vehiclesManager = new VehiclesMangagement(simulation);
        makeConnection();
    }
    //public static SumoTraciConnection conn;
    
    public VehiclesMangagement getVehiclesManager(){
        return vehiclesManager;
    }

    /**
    * Establishes connection
    */
    
    public  void makeConnection()  {





        /*String sumo_bin = "sumo-gui"; //Gibt Variable sumo_bin den Namen von Sumo-Gui
        String config_file = "src/main/resources/com/example/guifx/SumoTest.sumocfg"; //Sumo Config Datei
        double step_length = 0.1;*/

        new Thread(() -> {
            
            /**
            *
            *@throws
            */
            try {






                // do {conn.do_timestep(); } while ((int) conn.do_job_get(Vehicle.getIDCount())) > 0);
                // Auf Statistik Klasse zugreifen, die Methode zur Abfrage von Anzahl myVehicle Objekten in Simulation hat
                
                for (int i = 0; i < 200000; i++) {

                    simulation.doStep();

                    /*double timeSeconds = (double) conn.do_job_get(Simulation.getTime());
                    System.out.println("Current Time Stamp: " + timeSeconds);*/
                }

                simulation.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();   // <<<<< WICHTIG

    }

}
