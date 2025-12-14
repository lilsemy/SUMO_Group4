package com.example.guifx;

import org.eclipse.sumo.libtraci.Vehicle;
import org.eclipse.sumo.libtraci.GUI;
import org.eclipse.sumo.libtraci.TraCIPosition;

import java.util.*;

    /**
    *VehicleController is one of the sub controller classes that controls the vehicles in the simulation
    */

public class VehicleController {
    // for storing our vehicles in a map (dictionary);
    // key  = vehicle id
    // value = vehicle object
    private Map<String, VehicleModel> vehiclesList;
    private int vehicleCounter = 0;


    //removed Connection object, because it shouldn't see it
    public VehicleController() {
        this.vehiclesList = new HashMap<>();
    }


        /**
         *
         *@param typeId
         *@param routeId
         *@param laneId
         *@throws Exception
         */

    public VehicleModel createAndInjectVehicle(String typeId, String routeId, byte laneId) throws Exception {
            vehicleCounter++;
            String id = "id" + vehicleCounter;

            VehicleModel vehicle = new VehicleModel(id, typeId, routeId, laneId);
        // inject it into SUMO
            injectVehicle(vehicle);
        // return for GUI to track it
        return vehicle;
    }

    /**
    *
    *@param vehicle
    *@throws Exception
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
         *@return Speed of Vehicle
         *@param vehicleId
         *@throws Exception
         */

    public double getVehicleSpeed(String vehicleId) throws Exception {
            if (vehiclesList.containsKey(vehicleId)) {
                return vehiclesList.get(vehicleId).getSpeed();
            } else {
                System.out.println("Error! Vehicle " + vehicleId + " not found in simulation.");
                return 0;
            }
    }

    public String getLastVehicleId() {
            if (vehiclesList.isEmpty()) return null;
            return "id" + vehicleCounter; // letzte erzeugte ID
    }

     /**
     *
     * @return List of Vehicles
     */
    public Map<String, VehicleModel> getVehiclesMap() {
        return vehiclesList;
    }

     /**
    *@return vehicle
    *@param id
    */
    public VehicleModel getVehicle(String id) {//what
        return vehiclesList.get(id);
    }

     /**
    *@return sets camera of sumo-gui on given vehicle
    *@param viwId, vehId
    *@throws Exception
    */
    public void trackVehicle(String viwId, String vehId) throws Exception {

        if (getIds().contains(vehId)){
            //problem
            //GUI.trackVehicle(viwId, vehId);
        }
        else {
            System.out.println("Warning! Car left the Map or was deleted.");
        }
    }

        /**
         *
         * @return IDs of all Vehicle in the simulation
         * @throws Exception
         */
    public List<String> getIds() throws Exception{

        List<String> IDList =  Vehicle.getIDList();
        return IDList;
    }

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
