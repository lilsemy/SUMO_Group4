package com.example.guifx;



import it.polito.appeal.traci.SumoTraciConnection;
import de.tudresden.sumo.cmd.Vehicle;

import java.util.HashMap;
import java.util.Map;

// to hide the do job set...
import de.tudresden.sumo.cmd.Gui;

    /**
    *VehiclesManagement is a
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
    *@throws
    */
    public void injectVehicle(myVehicle v) throws Exception {
        SumoTraciConnection conn = simulation4.getConn();
        conn.do_job_set(Vehicle.add(v.getId(), v.getTypeId(), v.getRouteId(), v.getDepart(), v.getPos(), v.getSpeed(), v.getLaneId()));
        // save the vehicle in our map.
        vehicles.put(v.getId(), v);
    }

    
    /**
    *@return
    *@param id
    *@throws
    */
    public double getVehicleSpeed(String id) throws Exception {
        SumoTraciConnection conn = simulation4.getConn();
        return (double) conn.do_job_get(Vehicle.getSpeed(id));
    }


     /**
    *@return
    *@param id
    */
    public myVehicle getVehicle(String id) {//what
        return vehicles.get(id);
    }

     /**
    *@return
    *@param viwId, vehId
    *@throws
    */
    public void trackVehicle(String viwId, String vehId) throws Exception {
        SumoTraciConnection connection = simulation4.getConn();
        connection.do_job_set(Gui.trackVehicle(viwId, vehId));

    }


    //old
   /* private static int Idcounter = 1;
    private VehiclesMangagement (){}

    public static void injectCar(String givenRoutId, String givenTypeId) throws Exception{
        SumoTraciConnection conn = TraciConnect.getConn();
        String vehId = "V" + (Idcounter++);

        String defaultType = "testcar";
        String defaultRoute = "testroute ";


        String typeId;
        if (givenTypeId == null){
            typeId = defaultType;
        } else if (givenTypeId.isBlank()) {
            typeId = defaultType;

        }else {
            typeId = givenTypeId;
        }


        String routeId;
        if (givenRoutId == null){
            routeId = defaultRoute;
        } else if (givenRoutId.isBlank()) {
            routeId= defaultRoute;

        } else {
            routeId = givenRoutId;
        }

        int depart= (int) (double) conn.do_job_get(de.tudresden.sumo.cmd.Simulation.getTime());
        double pos = 0;
        double speed= 0;
        byte lane = 0;
        conn.do_job_set(Vehicle.add(vehId,typeId,routeId,depart,pos,speed,lane));
        System.out.println("Injected vehicle: " +vehId +", route: " + routeId );


    }
*/

}


