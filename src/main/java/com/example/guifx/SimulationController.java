package com.example.guifx;

/**
* SimulationController orchestrates the whole simulation
*/

public class SimulationController {
    private final SumoConnection connection;

    public SumoConnection getConnection() {
        return connection;
    }

    private final VehicleController vehicleController;
    //TODO
    //private final TrafficLightController tlController;
    //private final EdgeController edgeController;

    private int id = 0; //temporarily stores IDs of cars, probably needs to move elsewhere in the future
    /**
    *
    *@throws Exception
    */
    public SimulationController() throws Exception {
        this.connection = new SumoConnection();
        this.connection.connect();

        vehicleController = new VehicleController();
        //makeConnection(); happens in main
    }
    /**
    * Establishes connection
    */

    public void makeConnection(){
        new Thread(() -> {
            try {
                //Removed the statistics-bound loop, because this class should not have access to it
                while(true){ //TODO: This is an endless loop at the moment. Better have a boolean variable set to true and a function that sets the boolean variable to false in order to stop simulation
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

    //Originally in GUI, but created a new abstraction, since the GUI should only see the main controller
    public void spawnVehicle(){
        id += 1;
        String idf = "id" + id;
        byte lane1 = 0;
        /**
         *@throws Exception
         */

        try {
            //band-aid solution, only the vehicleController should create new Vehicle objects, not the main controller.
            //needs to be redone in VehicleController class
            VehicleModel car = new VehicleModel(idf,"car","route1",lane1);
            vehicleController.injectVehicle(car);
            vehicleController.trackVehicle("View #0",car.getId());
            System.out.println("add a car: " + car.getId());
            /*conn.do_job_set(Vehicle.add(idf, "car", "route1", 0, 0.0, 1.0, lane1)); //Fügt Lastwagen hinzu. ACHTUNG: VehicleType "ev"/"tr" muss in dem .rou.xml File definiert werden.
            conn.do_job_set(Gui.trackVehicle("View #0", idf));*/
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    //same story as with spawnVehicle
    public void getVehicleSpeed(){
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

    public VehicleController getVehicleController(){
        return vehicleController;
    }

}
