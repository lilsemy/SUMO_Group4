package com.example.guifx;

/*
Controller needs to pass SUMO simulation time from outside
import org.eclipse.sumo.libtraci.Simulation;
double currentTime = Simulation.getTime();
*/

import java.util.*;

/**
* Statistik is a model class to calculate relevant metrics over the simulation
*/

public class Statistik {

    private final VehicleController vehicleController;
    
    private Map<String, VehicleModel> currentVehicles = new HashMap<>();    // All vehicles in simulation
    private Map<String, Double> departureTimes = new HashMap<>();           // Departure times of vehicles
    private Map<String, Integer> vehicleCountsPerEdge = new HashMap<>();    // Vehicles per Edge (density)

    public Statistik(VehicleController vehicleController) {
        this.vehicleController = vehicleController;
    }

    public void updateVehicles(double currentTime) {
        this.currentVehicles = vehicleController.getVehicles(); //getVehicles() still needs to be implemented

        for (String id : currentVehicles.keySet()) {
            departureTimes.putIfAbsent(id, currentTime);
        }
    }

    /**
     * Calculates global average speed of all vehicles.
     */

    public double getAverageSpeed() {
        if (currentVehicles.isEmpty()) return 0;

        double sum = 0;
        for (VehicleModel v : currentVehicles.values()) {
            sum += v.getSpeed();
        }
        return sum / currentVehicles.size();
    }

    /**
     * Calculates how man vehicles there are per edge
     */

    public void updateVehicleDensity() {
        vehicleCountsPerEdge.clear();
        for (VehicleModel v : currentVehicles.values()) {
            String edgeId = v.getLaneId() + "";
            vehicleCountsPerEdge.putIfAbsent(edgeId, 0);
            vehicleCountsPerEdge.put(edgeId, vehicleCountsPerEdge.get(edgeId) + 1);
        }
    }

    /**
     * Calculates travel times for vehicles that have left the simulation
     */

    public Map<String, Double> calculateTravelTimes(double currentTime) {
        Map<String, Double> travelTimes = new HashMap<>();
        List<String> finishedVehicles = new ArrayList<>();

        for (String vehicleId : departureTimes.keySet()) {
            if (!currentVehicles.containsKey(vehicleId)) {
                double travelTime = currentTime - departureTimes.get(vehicleId);
                travelTimes.put(vehicleId, travelTime);
                finishedVehicles.add(vehicleId);
            }
        }

        // Remove vehicles that have left the simulation
        for (String vehicleId : finishedVehicles) {
            departureTimes.remove(vehicleId);
        }

        return travelTimes;
    }

    /**
     * Testing purposes -> Prints all statistics data.
     */
    public void printAllStatistics(double currentTime) {
    System.out.println("=== Simulation Statistics ===");
    System.out.println("Total vehicles: " + currentVehicles.size());
    System.out.println("Average speed: " + getAverageSpeed() + " m/s");
    System.out.println("Vehicle density per edge: " + vehicleCountsPerEdge);
    System.out.println("Travel times (for vehicles that have left the simulation): " + calculateTravelTimes(currentTime));
    }
}


