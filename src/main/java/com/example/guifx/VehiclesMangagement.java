package com.example.guifx;

import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

// to hide the do job set...
import de.tudresden.sumo.cmd.Gui;
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
        this.vehicles = new HashMap<String, myVehicle>();
    }

    
    /**
    *
    *@param v
    *@throws Exception
    */
    public void injectVehicle(myVehicle v) throws Exception {
        SumoTraciConnection conn = simulation4.getConn();
        conn.do_job_set(Vehicle.add(v.getId(), v.getTypeId(), v.getRouteId(), v.getDepart(), v.getPos(), v.getSpeed(), v.getLaneId()));
        // save the vehicle in our map.
        vehicles.put(v.getId(), v);
    }

    
    /**
    *@return Speed of Vehicle
    *@param id
    *@throws Exception
    */
    public double getVehicleSpeed(String id) throws Exception {
        SumoTraciConnection conn = simulation4.getConn();
        if (getIds().contains(id)){
            return (double) conn.do_job_get(Vehicle.getSpeed(id)); }
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
        SumoTraciConnection connection = simulation4.getConn();
        connection.do_job_set(Gui.trackVehicle(viwId, vehId));

    }

        /**
         *
         * @return IDs of all Vehicle in the simulation
         * @throws Exception
         */
    public List<String> getIds() throws Exception{
        SumoTraciConnection connection = simulation4.getConn();
        List<String> IDList = (List<String>) connection.do_job_get(Vehicle.getIDList());
        return IDList;
    }

}


