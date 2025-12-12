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


    //removed Connection object, because it shouldn't see it
    public VehicleController() {
        this.vehiclesList = new HashMap<>();
    }

    
    /**
    *
    *@param v
    *@throws Exception
    */
    public void injectVehicle(VehicleModel v) throws Exception {

        try {
        Vehicle.add(v.getId(), v.getRouteId(), v.getTypeId(),
                    String.valueOf(v.getDepart()),
                    String.valueOf(v.getLaneId()),
                    String.valueOf(v.getPos()),
                    String.valueOf(v.getSpeed()),
                    String.valueOf(v.getLaneId()));
        // save the vehicle in our map.
        vehiclesList.put(v.getId(), v);
        System.out.println("Injected vehicle: " + v.getId());
            
        } catch(Exception e) {
            System.err.println("Failed to inject vehicle " + v.getId() + ": " + e.getMessage());
        }
    }

    /**
     *
     * @return List of Vehicles
     */
    public Map<String, VehicleModel> getVehiclesMap() {
        return vehiclesList;
    }

    /**
    *@return Speed of Vehicle
    *@param id
    *@throws Exception
    */
    public double getVehicleSpeed(String id) throws Exception {

        if (getIds().contains(id)){
            return vehiclesList.get(id).getSpeed(); }
        else {
            System.out.println("Error! Car not found!");
            return 0;
        }
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

    public void updateFromSimulation() throws Exception{

        for (String id : Vehicle.getIDList()){
            double speed = Vehicle.getSpeed(id);
            TraCIPosition pos = Vehicle.getPosition(id,false);
            double angle = Vehicle.getAngle(id);

            // Get our local object OR create a new one if needed
            VehicleModel v = vehiclesList.getOrDefault(id , new VehicleModel(id));

            v.setSpeed(speed);
            v.setPosition(pos.getX(),pos.getY());
            v.setAngle(angle);

            vehiclesList.put(id, v);

        }

    }

}


