package com.example.guifx;

import org.eclipse.sumo.libtraci.Vehicle;
import org.eclipse.sumo.libtraci.GUI;
import org.eclipse.sumo.libtraci.TraCIPosition;

import java.util.HashMap;
import java.util.Map;
import java.util.List;


import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;

/**
    *VehiclesManagement is the class that controls the Vehicles in the simulation
    */

public class VehiclesMangagement {
    private TraciConnect simulation4;


    // for storing our vehicles in a map (dictionary);
    // key  = vehicle id
    // value = vehicle object
    private Map<String, myVehicle> vehicles;


    /**
    *
    *@param simulation
    */
    public VehiclesMangagement(TraciConnect simulation) {
        this.simulation4 = simulation;
        this.vehicles = new HashMap<>();
    }

    
    /**
    *
    *@param v
    *@throws Exception
    */
    public void injectVehicle(myVehicle v) throws Exception {
        Vehicle.add()
        Vehicle.add(v.getId(), v.getRouteId(), v.getTypeId(),String.valueOf(v.getDepart()),String.valueOf(v.getLaneId()), String.valueOf(v.getPos()), String.valueOf(v.getSpeed()), String.valueOf(v.getLaneId()));
        // save the vehicle in our map.
        vehicles.put(v.getId(), v);
    }

    
    /**
    *@return Speed of Vehicle
    *@param id
    *@throws Exception
    */
    public double getVehicleSpeed(String id) throws Exception {

        if (getIds().contains(id)){
            return Vehicle.getSpeed(id); }
        else {
            System.out.println("Error! Car not found!");
            return 0;
        }
    }


     /**
    *@return vehicle
    *@param id
    */
    public myVehicle getVehicle(String id) {//what
        return vehicles.get(id);
    }

     /**
    *@return sets camera of sumo-gui on given vehicle
    *@param viwId, vehId
    *@throws Exception
    */
    public void trackVehicle(String viwId, String vehId) throws Exception {

        GUI.trackVehicle(viwId, vehId);

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
            myVehicle v = vehicles.getOrDefault(id , new myVehicle(id));

            v.setSpeed(speed);
            v.setPosition(pos.getX(),pos.getY());
            v.setAngle(angle);

            vehicles.put(id, v);

        }



    }
}


