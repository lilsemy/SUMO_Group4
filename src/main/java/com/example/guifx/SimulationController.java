package com.example.guifx;

/**
* SimulationController orchestrates the whole simulation
*/

public class SimulationController {
    private final SumoConnection connection;
    private final VehicleController vehicleController;
    private final TrafficLightController tlController;

    //TODO
    //private final EdgeController edgeController;

    private volatile boolean running = true;   // starts true (volatile booleans have multi-thread visibility)

    private int id = 0; //temporarily stores IDs of cars, probably needs to move elsewhere in the future
    /**
    *
    *@throws Exception
    */
    public SimulationController() throws Exception {
        this.connection = new SumoConnection();
        this.connection.connect();

        vehicleController = new VehicleController();
        tlController = new TrafficLightController();
        //makeConnection(); happens in main
    }
    /**
    * Establishes connection
    */

    public void makeConnection(){
        /*
         * EXPLANATION (Why this is commented out):
         * Originally, this method started a separate background Thread to run the
         * simulation loop continuously.
         * However, when rendering the simulation in JavaFX (the GUI), we must update
         * the screen on the "JavaFX Application Thread".
         * If we have a background thread like this running 'while(running)', it often
         * runs too fast or desynchronized from the screen refresh rate (60fps).
         * Worse, updating UI elements from this background thread would cause
         * "Not on FX Application Thread" errors.
         *
         * SOLUTION:
         * We disable this loop. Instead, the 'GUI' class controls the loop using an
         * 'AnimationTimer'.
         * The GUI calls 'singleStep()' once per frame. This ensures the simulation
         * advances exactly one step before we try to draw it.
         */

        /*new Thread(() -> {
            try {
                
                while(running){
                    connection.doStep();
                    //vehicleController.updateCars or similar method needs to be implemented (also for every other controller)
                }
            } catch (Exception e) {
                //System.err.println("Error during simulation step: " + stepEx.getMessage());
                //stepEx.printStackTrace();
                // Optionally: pause simulation or notify GUI
                running = false;
            } finally {
                //the finally block ALWAYS executes: if try finishes; catch triggers or not; exception is thrown
                connection.close();
                System.out.println("Simulation stopped cleanly.");
            }
        }).start();*/
    }
    /**
     * EXPLANATION:
     * This method advances the simulation by exactly one step (0.05s).
     * It is called by the GUI's AnimationTimer loop.
     * Useing this instead of the while-loop in makeConnection so the GUI stays
     * responsive and synchronized.
     */
    public void singleStep() throws Exception{
        if (running && connection.isConnected()){
            connection.doStep();
        }
    }

    /**
     * Stops the simulation loop cleanly.
     */
    public void stopSimulation() {
        running = false;     // shutdown simulation
    }

    /**
     * Restarts the simulation loop after stopping.
     */
    public void startSimulation() {
        if (running) return; // already running

        running = true;
        makeConnection();    // start new thread
    }

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
            VehicleModel car = new VehicleModel(idf,"car","r1",lane1);
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

    public void changePhase(){
        tlController.changePhase();
    }

    public VehicleController getVehicleController(){
        return vehicleController;
    }

    public TrafficLightController getTlController() {
        return tlController;
    }
    public SumoConnection getConnection() {
        return connection;
    }

}
