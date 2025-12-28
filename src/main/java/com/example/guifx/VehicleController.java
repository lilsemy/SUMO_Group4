package com.example.guifx;

import org.eclipse.sumo.libtraci.Vehicle;
import org.eclipse.sumo.libtraci.GUI;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Simulation;

import java.util.*;

/**
 * VehicleController controls vehicles in the simulation
 */
public class VehicleController {
    private Map<String, VehicleModel> vehiclesList;
    private int vehicleCounter = 0;

    /**
     * Constructs a VehicleController
     */
    public VehicleController() {
        this.vehiclesList = new HashMap<>();
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
    public VehicleModel createAndInjectVehicle(String typeId, String routeId, byte laneId) throws Exception {
        vehicleCounter++;
        String id = "id" + vehicleCounter;

        double currentTime = Simulation.getTime();

        VehicleModel vehicle = new VehicleModel(id, typeId, routeId, laneId, currentTime);

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
                    String.valueOf(vehicle.getLaneId()),
                    String.valueOf(vehicle.getPos()),
                    String.valueOf(vehicle.getSpeed()),
                    String.valueOf(vehicle.getLaneId()));

            vehiclesList.put(vehicle.getId(), vehicle);
            System.out.println("Injected vehicle: " + vehicle.getId());
        } catch (Exception e) {
            System.err.println("Failed to inject vehicle " + vehicle.getId() + ": " + e.getMessage());
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
            System.out.println("Error! Vehicle " + vehicleId + " not found in simulation.");
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
    public void trackVehicle(String viwId, String vehId) throws Exception {
        if (getIds().contains(vehId)) {
            // GUI.trackVehicle(viwId, vehId); // commented out
        } else {
            System.out.println("Warning! Car left the Map or was deleted.");
        }
    }

    /**
     * Returns IDs of all vehicles in the simulation
     * 
     * @return List of vehicle IDs
     * @throws Exception if retrieval fails
     */
    public List<String> getIds() throws Exception {
        List<String> IDList = Vehicle.getIDList();
        return IDList;
    }

    /**
     * Updates the local vehicle states from the simulation
     * 
     * @throws Exception if update fails
     */
    public void updateFromSimulation() throws Exception {
        List<String> liveIds = Vehicle.getIDList();

        vehiclesList.keySet().retainAll(liveIds);

        for (String id : liveIds) {
            double speed = Vehicle.getSpeed(id);
            TraCIPosition pos = Vehicle.getPosition(id, false);
            double angle = Vehicle.getAngle(id);

            VehicleModel v = vehiclesList.getOrDefault(id, new VehicleModel(id));

            v.setSpeed(speed);
            v.setPosition(pos.getX(), pos.getY());
            v.setAngle(angle);

            vehiclesList.put(id, v);
        }
    }

    //filtering
    public Collection<VehicleModel> getVehiclesByType(TypeFilter filter) {
        Collection<VehicleModel> all = vehiclesList.values();

        if (filter == TypeFilter.NONE) {
            return all;
        }

        String selectedType = filter.getTypeId();

        return all.stream()
                .filter(v -> selectedType.equals(v.getTypeId()))
                .toList();
    }
}
