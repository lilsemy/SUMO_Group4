package com.example.guifx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.sumo.libtraci.Simulation;
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
    private static final Logger LOG = LogManager.getLogger(SimulationController.class.getName());
    private volatile boolean running = true;
    private static List<String> routes = List.of("r1", "r2", "r3", "r4", "r6", "r7", "r8", "r9", "r10",
            "r11", "r12", "r13", "r14", "r16", "r17", "r18", "r19", "r20", "r21", "r22", "r23");
    private Random random = new Random();
    private final int MAX_VEHICLES = 15000;

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
        LOG.info("Simulation Controller initialized successfully");
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
            vehicleController.updateFromSimulation();
        }
    }

    /**
     * Method that lets the user perform a stress test
     * @param count the amount of cars for the stress test
     */

    public int startStressTest(int count, SpawnConfig config) {
        int currentVehicles = vehicleController.getVehiclesMap().size();
        int allowedToSpawn = MAX_VEHICLES - currentVehicles;

        if (allowedToSpawn <= 0){
            LOG.warn("Stress test refused: Vehicle limit of " + MAX_VEHICLES + " reached.");
            return 0;
        }
        // If the user wants 1000 cars, but we only have space for 50,
        // we only spawn 50. We take the smaller number.
        int actualSpawnCount = Math.min(count, allowedToSpawn);
        for(int i = actualSpawnCount; i>0; i--){
            TypeFilter type = config.pickType();
            VehicleColor color = config.pickColor();
            try{
                vehicleController.createAndInjectVehicle(type.getTypeId(), pickRoute(), "0", color);
            } catch (Exception e) {
                LOG.error("Starting of Stress Test failed: " + e.getMessage());
                e.printStackTrace();
            }

        }
        return actualSpawnCount;
    }

    /**
     * Stops the simulation loop cleanly.
     */
    public void stopSimulation() {
        running = false;     // shutdown simulation
    }

    /**
     * Changes type and color values of a vehicle
     * @param id vehicleId
     * @param newType new Type
     * @param newColor new Color
     */

    public void changeVehicleAppearance(String id, TypeFilter newType, VehicleColor newColor){
        if(newType == TypeFilter.NONE && newColor == VehicleColor.NONE){
            LOG.error("Select a vehicle type or color before attempting editing!");
            return;
        }

        VehicleModel v = vehicleController.getVehicle(id);
        if (newType != TypeFilter.NONE) {
            v.setTypeId(newType.getTypeId());
        }
        if (newColor != VehicleColor.NONE) {
            v.setColor(newColor);
        }
    }

    /**
     * Spawns a new vehicle and sets its attributes and starting lane
     * @param config allowed vehicle attributes
     * @param lane starting lane
     * @return
     */
    public String spawnVehicle(SpawnConfig config, String lane){
        try {
            TypeFilter type = config.pickType();
            VehicleColor color = config.pickColor();

            VehicleModel v = vehicleController.createAndInjectVehicle(
                    type.getTypeId(),
                    pickRoute(),
                    "0",
                    color
            );

            if(lane != null) {
                //Random Edge chosen from List of all Edges, that are leaving the Map -> Endpoint of newly created Route
                String target = laneCon.getEndLanes().get(new Random().nextInt(laneCon.getEndLanes().size()));

                var route = Simulation.findRoute(laneCon.getLaneModel(lane).getEdge(), target);

                vehicleController.setRoute(v.getId(), route.getEdges());

                vehicleController.moveToLane(v.getId(), lane);
            }
            return v.getId();

        } catch (Exception e) {
            LOG.error("Spawning of Vehicle failed: " + e.getMessage());
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
