package com.example.guifx;

/**
* Connection is running the Simulation with SUMO
*/

public class Connection {
    private TraciConnect simulation;
    private VehiclesMangagement vehiclesManager;

    private GUI controller1;

    /**
    *
    *@throws Exception
    */
    
    public Connection() throws Exception {
        simulation = new TraciConnect();
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
            *@throws Exception
            */
            try {
                Statistik stat = new Statistik();
                simulation.doStep();
                stat.setVehicleIds(simulation, vehiclesManager);

                do {
                    simulation.doStep();
                } while (stat.getCars() > 0);

                simulation.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();   // <<<<< WICHTIG

    }

}
//        Statistik stat = new Statistik();
//        stat.getStatistik();