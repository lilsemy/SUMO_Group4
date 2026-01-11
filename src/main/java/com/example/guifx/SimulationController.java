package com.example.guifx;

import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.Vehicle;

import java.util.List;
import java.util.Random;

/**
* SimulationController orchestrates the whole simulation
*/

public class SimulationController {
    private final SumoConnection connection;
    private final VehicleController vehicleController;
    private final TrafficLightController tlController;
    private final LaneController laneCon;

    private volatile boolean running = true;   // starts true (volatile booleans have multi-thread visibility)

    //Stress test counters
    private int stressVehiclesRemaining = 0;
    private int stepCounter = 0;
    private int injectionStepInterval = 10; // every 10 steps -> 10*0.05s/step = every 0.5s
    private boolean stressTestActive = false;

    private static List<String> routes = List.of("r1", "r2", "r3", "r4", "r6", "r7", "r8", "r9", "r10",
            "r11", "r12", "r13", "r14", "r15", "r16", "r17", "r18", "r19", "r20", "r21", "r22", "r23");
    private Random random = new Random();
    /**
    *
    *@throws Exception
    */
    public SimulationController() throws Exception {
        this.connection = new SumoConnection();
        this.connection.connect();

        vehicleController = new VehicleController();
        tlController = new TrafficLightController();
        laneCon = new LaneController();
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
     *
     */
    public void singleStep() throws Exception{
        if (running && connection.isConnected()){
            connection.doStep();
            stepCounter++;

            //stress testing only triggers when stressTestActive == true
            /*if (stressTestActive &&
                    stressVehiclesRemaining > 0 &&
                    stepCounter % injectionStepInterval == 0) {

                try {
                    // Changed from (byte) 0 -> "0"
                    vehicleController.createAndInjectVehicle(
                            "car", "r1", "0", VehicleColor.BLACK
                    );

                    stressVehiclesRemaining--;

                    if (stressVehiclesRemaining == 0) {
                        stressTestActive = false;
                    }

                } catch (Exception e) {
                    System.err.println(
                            "Stress test injection failed, remaining="
                                    + stressVehiclesRemaining
                    );
                    e.printStackTrace();
                }
            }
            */
            vehicleController.updateFromSimulation();
        }
    }

    /**
     * Method that lets the user perform a stress test
     * @param count the amount of cars for the stress test
     */

    public void startStressTest(int count, SpawnConfig config) {
        /*stressVehiclesRemaining = count;
        stressTestActive = true;
        stepCounter = 0;*/


        for(int i = count; i>0; i--){
            TypeFilter type = config.pickType();
            VehicleColor color = config.pickColor();
            try{
                vehicleController.createAndInjectVehicle(type.getTypeId(), pickRoute(), "0", color);
            } catch (Exception e) {
                e.printStackTrace();
            }

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

    public String spawnVehicle(SpawnConfig config, String lane){
        try {
            TypeFilter type = config.pickType(); //currently the config is set to cars, because I only have all the images for them
            VehicleColor color = config.pickColor();

            VehicleModel v = vehicleController.createAndInjectVehicle(
                    type.getTypeId(),
                    pickRoute(),
                    "0",
                    color
            );

            if(lane != null) {
                //Random Edge choosen from List of all Edges, that are leaving the Map -> Endpoint of newly created Route
                String target = laneCon.getEndLanes().get(new Random().nextInt(laneCon.getEndLanes().size()));

                //Calculate a route from the User selected Edge, to the just choosen End Edge
                var route = Simulation.findRoute(laneCon.getLaneModel(lane).getEdge(), target);

                //Change Route of Vehicle to new Route
                vehicleController.setRoute(v.getId(), route.getEdges());

                //Teleport Vehicle to actual Lane, which was selected
                vehicleController.moveToLane(v.getId(), lane);
            }
            return v.getId();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String pickRoute(){
        return routes.get(random.nextInt(routes.size()));
    }

    public void changePhase(double duration, String GroupID){
        tlController.changePhase(duration, GroupID);
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
    public LaneController getLaneController(){return laneCon;}
    public double getTime() {
        return org.eclipse.sumo.libtraci.Simulation.getTime();
    }

}
