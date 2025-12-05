package com.example.guifx;

/**
* Connection is running the Simulation with SUMO
*/

public class SimulationController {
    private final SumoConnection connection;
    private final VehicleController vehicleController;
    /**
    *
    *@throws Exception
    */
    public SimulationController() throws Exception {
        this.connection = new SumoConnection();
        this.connection.connect();

        vehicleController = new VehicleController();
        makeConnection();
    }
    /**
    * Establishes connection
    */

    public void makeConnection(){
        new Thread(() -> {
            try {
                //Removed the statistics-bound loop, because this class should not have access to it
                while(true){
                    connection.doStep();
                    //vehicleController.updateCars or similar method needs to be implemented (also for every other controller)
                }
            } catch (Exception e) {
                //eventually we need better error handling, fine for now
                e.printStackTrace();
            } finally {
                //the finally block ALWAYS executes: if try finishes; catch triggers or not; exception is thrown
                //It closes the thread cleanly, whatever happens. So no zombie simulation, leaky sumo process, or left open library
                connection.close();
            }
        }).start();
    }
//old method, feel free to delete
//    public  void makeConnection()  {
//        new Thread(() -> {
//            /**
//            *
//            *@throws Exception
//            */
//            try {
//                Statistik stat = new Statistik();
//                connection.doStep();
//                stat.setVehicleIds(vehicleController);
//
//                do {
//                    connection.doStep();
//                } while (stat.getCars() > 0);
//
//                connection.close();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//        }).start();
//
//    }

    public VehicleController getVehicleController(){
        return vehicleController;
    }

}
