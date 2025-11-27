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
    
    public VehiclesMangagement getVehiclesManager(){
        return vehiclesManager;
    }

    /**
    * Establishes connection
    */
    
    public  void makeConnection()  {
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
        }).start();

    }

}
//        Statistik stat = new Statistik();
//        stat.getStatistik();