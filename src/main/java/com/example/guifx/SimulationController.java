package com.example.guifx.controller;

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
    public void spawnVehicle() {
        try {
            // Delegate vehicle creation and injection entirely to VehicleController
            VehicleModel car = vehicleController.createAndInjectVehicle("car", "r1", (byte)0);

            // Optionally track the vehicle in SUMO GUI
            vehicleController.trackVehicle("View #0", car.getId());

            System.out.println("Added a car: " + car.getId());
        } catch (Exception ex) {
            ex.printStackTrace();
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
