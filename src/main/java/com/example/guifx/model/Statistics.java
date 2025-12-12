package com.example.guifx.model;

import com.example.guifx.controller.SimulationController;
/*
Controller needs to pass SUMO simulation time from outside
import org.eclipse.sumo.libtraci.Simulation;
double currentTime = Simulation.getTime();
*/

import java.util.*;

/**
* Statistics is a model class to calculate relevant metrics over the simulation
*/

public class Statistics {

    private final SimulationController simCon;

    //Vehicle Data Structure
    private Map<String, VehicleModel> currentVehicles = new HashMap<>();    // All vehicles in simulation
    private Map<String, Double> departureTimes = new HashMap<>();           // Departure times of vehicles
    private Map<String, Integer> vehicleCountsPerEdge = new HashMap<>();    // Vehicles per Edge (density)

    // Traffic Light Data Structure
    private Map<String, Integer> currentTrafficLightStates = new HashMap<>(); // Count of Traffic lights per phase
    
    public Statistics(SimulationController simCon) {
        this.simCon = simCon;
    }

    /**
    * Refresh vehicles list at a point in time of simulation
    */
    public void updateVehicles(double currentTime) {
        this.currentVehicles = simCon.getVehicleController().getVehiclesMap(); //getVehicles() still needs to be implemented

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
     * Calculates how many vehicles there are per edge
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

        for (Map.Entry<String, Double> entry : departureTimes.entrySet()) {
            String vehicleId = entry.getKey();
            double departureTime = entry.getValue();

            if (!currentVehicles.containsKey(vehicleId)) {
                double travelTime = currentTime - departureTime;
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
    * Fetches the current state of all traffic lights from the simulation and counts how many are currently red, yellow, or green
    */
    public void updateTrafficLights() {
    Map<String, TrafficLightModel> currentTrafficLights;

    currentTrafficLights = simCon.getTlController().getTlList();

    Map<String, Integer> counts = new HashMap<>();
    counts.put("R", 0);
    counts.put("Y", 0);
    counts.put("G", 0);

    for (TrafficLightModel tl : currentTrafficLights.values()) {
        String state = tl.getRedYellowGreenState().toUpperCase();

        if (state.contains("R")) counts.put("R", counts.get("R") + 1);
        if (state.contains("Y")) counts.put("Y", counts.get("Y") + 1);
        if (state.contains("G")) counts.put("G", counts.get("G") + 1);
        }

    this.currentTrafficLightStates = counts;
    }


    /**
     * Testing purposes -> Prints all statistics data.
     */
    public void printAllStatistics(double currentTime) {
    
    updateVehicles(currentTime);
    updateVehicleDensity();
    updateTrafficLights();    
        
    System.out.println("=== Simulation Statistics ===");
    System.out.println("Total vehicles: " + currentVehicles.size());
    System.out.println("Average speed: " + getAverageSpeed() + " m/s");
    System.out.println("Vehicle density per edge: " + vehicleCountsPerEdge);
    System.out.println("Travel times (for vehicles that have left the simulation): " + calculateTravelTimes(currentTime));
    System.out.println("Traffic lights count per color: " + currentTrafficLightStates);
    }
}

