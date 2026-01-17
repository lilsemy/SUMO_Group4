package com.example.guifx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.sumo.libtraci.StringVector;
import org.eclipse.sumo.libtraci.Vehicle;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Simulation;

import java.util.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * VehicleController controls vehicles in the simulation
 */
public class VehicleController {
    private Map<String, VehicleModel> vehiclesList;
    private int vehicleCounter = 0;
    private int CurrentVehicles = 0;
    private static final Logger LOG = LogManager.getLogger(VehicleController.class.getName());

    /**
     * Constructs a VehicleController
     */
    public VehicleController() {
        this.vehiclesList = new ConcurrentHashMap<>();
    }

    /**
     * Creates and injects a vehicle into the simulation
     * 
     * @param typeId  vehicle type ID
     * @param routeId route ID
     * @param laneId  lane index
     * @return VehicleModel created
     * @throws Exception if injection fails
     */
    public VehicleModel createAndInjectVehicle(String typeId, String routeId, String laneId, VehicleColor color) throws Exception {
        vehicleCounter++;
        String id = "id" + vehicleCounter;

        double currentTime = Simulation.getTime();

        VehicleModel vehicle = new VehicleModel(id, typeId, routeId, laneId, currentTime, color);

        injectVehicle(vehicle);
        return vehicle;
    }

    /**
     * Injects a vehicle into the SUMO simulation
     * 
     * @param vehicle VehicleModel to inject
     * @throws Exception if injection fails
     */
    public void injectVehicle(VehicleModel vehicle) throws Exception {
        try {
            Vehicle.add(vehicle.getId(), vehicle.getRouteId(), vehicle.getTypeId(),
                    String.valueOf(vehicle.getDepart()),
                    vehicle.getLaneId(),
                    String.valueOf(vehicle.getPos()),
                    String.valueOf(vehicle.getSpeed()),
                    vehicle.getLaneId());

            vehiclesList.put(vehicle.getId(), vehicle);

            LOG.info("Injected vehicle: " + vehicle.getId());
        } catch (Exception e) {
            LOG.warn("Failed to inject vehicle " + vehicle.getId() + ": " + e.getMessage());
        }
    }


    /**
     * Returns the speed of a vehicle
     * 
     * @param vehicleId ID of the vehicle
     * @return speed of vehicle or 0 if not found
     * @throws Exception if retrieval fails
     */
    public double getVehicleSpeed(String vehicleId) throws Exception {
        if (vehiclesList.containsKey(vehicleId)) {
            return vehiclesList.get(vehicleId).getSpeed();
        } else {
            LOG.warn("Error! Vehicle " + vehicleId + " not found in simulation.");
            return 0;
        }
    }

    /**
     * Returns the last created vehicle ID
     * 
     * @return last vehicle ID or null
     */
    public String getLastVehicleId() {
        if (vehiclesList.isEmpty()) return null;
        return "id" + vehicleCounter;
    }

    /**
     * Teleports Vehicle to given Lane.
     * @param vehID
     * @param lane
     */
    public void moveToLane(String vehID, String lane){
        Vehicle.moveTo(vehID, lane, 0);
    }

    /**
     * Sets Vehicles Route to newly given Route.
     * @param vehId
     * @param route
     */
    public void setRoute(String vehId, StringVector route){
        VehicleModel v = getVehicle(vehId);
        v.setRouteId("c"); //"c" stands for custom RouteID, since we calculate a random one, which is not saved in rou.xml -> has no ID
        Vehicle.setRoute(vehId, route);
    }

    /**
     * Returns a map of all vehicles
     * 
     * @return Map of vehicle IDs to VehicleModel
     */
    public Map<String, VehicleModel> getVehiclesMap() {
        return vehiclesList;
    }

    /**
     * Returns the vehicle with the given ID
     * 
     * @param id vehicle ID
     * @return VehicleModel or null if not found
     */
    public VehicleModel getVehicle(String id) {
        return vehiclesList.get(id);
    }

    /**
     * Sets the camera to track a specific vehicle
     * 
     * @param viwId view ID
     * @param vehId vehicle ID
     * @throws Exception if tracking fails
     */
    /*public void trackVehicle(String viwId, String vehId) throws Exception {
        if (getIds().contains(vehId)) {
            // GUI.trackVehicle(viwId, vehId); // commented out
        } else {
            System.out.println("Warning! Car left the Map or was deleted.");
        }
    }*/

    /**
     * Returns IDs of all vehicles in the simulation
     * 
     * @return List of vehicle IDs
     * @throws Exception if retrieval fails
     */
    public List<String> getIds() throws Exception {
        //List<String> IDList = Vehicle.getIDList();
        return Vehicle.getIDList();
    }



    private final long UI_UPDATE_NANOS = 75_000_000L;
    private long lastUiNow = 0L;

    /**
     * Updates the local vehicle states from the simulation
     * 
     * @throws Exception if update fails
     */

    public void updateFromSimulation() throws Exception {



            long now = System.nanoTime();
            if (now - lastUiNow < UI_UPDATE_NANOS) {
                return; // keep last cached values
            }
            lastUiNow = now;


            Set<String> liveIds = new HashSet<>(Vehicle.getIDList());

            for (VehicleModel v : vehiclesList.values()) {
                String id = v.getId();

                if (liveIds.contains(id)) {
                    v.setState(VehicleState.ACTIVE);

                    TraCIPosition pos = Vehicle.getPosition(id);
                    v.setPosition(pos.getX(), pos.getY());
                    v.setSpeed(Vehicle.getSpeed(id));
                    v.setAngle(Vehicle.getAngle(id));
                    v.setLaneId(Vehicle.getLaneID(id));
                }
            }

            vehiclesList.values().removeIf(vm ->
                    vm.getState() == VehicleState.ACTIVE &&
                            !liveIds.contains(vm.getId())
            );

            CurrentVehicles = Vehicle.getIDCount();


//        Set<String> liveIds = new HashSet<>(Vehicle.getIDList());
//
//        for(VehicleModel v : vehiclesList.values()){
//
//            if(liveIds.contains(v.getId())){
//                v.setState(VehicleState.ACTIVE);
//
//                TraCIPosition pos = Vehicle.getPosition(v.getId());
//
//                v.setPosition(pos.getX(),pos.getY());
//                v.setSpeed(Vehicle.getSpeed(v.getId()));
//                v.setAngle(Vehicle.getAngle(v.getId()));
//                v.setLaneId(Vehicle.getLaneID(v.getId()));
//            }
//
//        }
//        //if vehicle is flagged as ACTIVE and left the simulation, delete it.
//        vehiclesList.values().removeIf(vehicleModel ->
//                                        vehicleModel.getState() == VehicleState.ACTIVE &&
//                                                !(liveIds.contains(vehicleModel.getId())));

    }

    //filtering
    public Collection<String> getFilteredVehicleIds(TypeFilter typeFilter, VehicleColor colorFilter) {
        Collection<String> result = new ArrayList<>();

        for(VehicleModel v : vehiclesList.values()){
            //does the type of v match with the given filter? TRUE, if not, FALSE
            boolean typeMatches = typeFilter == TypeFilter.NONE ||
                                  typeFilter.getTypeId().equals(v.getTypeId());
            //does the color of v match with the given filter? TRUE, if not, FALSE
            boolean colorMatches = colorFilter == VehicleColor.NONE ||
                                   colorFilter == v.getColor();
            //if both true, add to list
            boolean isActive = v.getState() == VehicleState.ACTIVE;
            if (typeMatches && colorMatches && isActive){
                result.add(v.getId());}

        }

        return result;
    }

    public int getCurrentVehicles() {
        return CurrentVehicles;
    }

}
