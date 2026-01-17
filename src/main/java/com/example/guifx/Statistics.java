package com.example.guifx;

import java.util.*;

/**
* Statistics is a model class to calculate relevant metrics over the simulation
*/

public class Statistics {

    private final SimulationController simCon;
    private Map<String, VehicleModel> currentVehicles = new HashMap<>();
    private Map<String, Double> departureTimes = new HashMap<>();
    private double totalTravelTime = 0.0;
    private int finishedVehicleCount = 0;
    private Map<String, Integer> currentTrafficLightStates = new HashMap<>();

    public Statistics(SimulationController simCon) {
        this.simCon = simCon;
    }

    /**
    * Refresh vehicles list at a point in time of simulation
    */
    public void updateVehicles(double currentTime) {
    Map<String, VehicleModel> latest = simCon.getVehicleController().getVehiclesMap();

    currentVehicles = latest;

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

        for (String vehicleId : finishedVehicles) {
            departureTimes.remove(vehicleId);
        }

        return travelTimes;
    }

    /**
     * Returns the real-time average travel time of all vehicles:
     *  - finished vehicles contribute their final travel time
     *  - active vehicles contribute time spent so far
     */
    public double updateAndGetAverageTravelTime(double currentTime) {

        Map<String, Double> finishedTravelTimes = calculateTravelTimes(currentTime);

        for (double travelTime : finishedTravelTimes.values()) {
            totalTravelTime += travelTime;
            finishedVehicleCount++;
        }

        double activeTravelTimeSum = 0.0;

        for (Map.Entry<String, Double> entry : departureTimes.entrySet()) {
            double departureTime = entry.getValue();
            activeTravelTimeSum += (currentTime - departureTime);
        }

        int activeVehicleCount = departureTimes.size();
        int totalVehicleCount = finishedVehicleCount + activeVehicleCount;

        if (totalVehicleCount == 0) {
            return 0.0;
        }

        return (totalTravelTime + activeTravelTimeSum) / totalVehicleCount;
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

    public Map<String, Double> detectCongestionHotspots(double currentTime) {

        updateVehicles(currentTime);

        Map<String, List<Double>> speedsPerLane = new HashMap<>();
        Map<String, Double> congestedLanes = new HashMap<>();

        for (VehicleModel v : currentVehicles.values()) {

            String laneId = v.getLaneId();
            double speed = v.getSpeed();

            speedsPerLane
                    .computeIfAbsent(laneId, k -> new ArrayList<>())
                    .add(speed);
        }

        for (Map.Entry<String, List<Double>> entry : speedsPerLane.entrySet()) {

            List<Double> speeds = entry.getValue();
            if (speeds.size() < 2) continue;

            double sum = 0.0;
            for (double s : speeds) {
                sum += s;
            }

            double avgSpeed = sum / speeds.size();

            if (avgSpeed < 1.0) {
                congestedLanes.put(entry.getKey(), avgSpeed);
            }
        }

        return congestedLanes;
    }

    public int getVehicleCount() {
        return simCon.getVehicleController().getCurrentVehicles();
    }

    public boolean isCongestionPresent(double currentTime) {
        return !detectCongestionHotspots(currentTime).isEmpty();
    }

    public Map<String, Integer> getTrafficLightStates() {
        return currentTrafficLightStates;
    }
}

