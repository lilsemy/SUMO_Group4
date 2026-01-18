package com.example.guifx;
import java.util.List;



/**
 * SimSnapshot is a Record that holds ALL data required to render one frame on
 * the screen.
 * It acts as a thread-safe data transfer object (DTO) between the Simulation
 * hread (producer)
 * and the JavaFX UI Thread (consumer)
 * @param time          The current simulation time in seconds.
 * @param vehicles      A list of VehicleUiState checks representing all
 *                      active vehicles.
 * @param avgSpeed      The average speed of all vehicles in this frame.
 * @param avgTravelTime The average travel time statistics.
 * @param count         The total count of active vehicles.
 * @param trafficLights A list of TrafficLightUIState representing all
 *                      traffic lights.
 */

public record SimSnapshot (double time, List <VehicleUiState> vehicles, double avgSpeed,double avgTravelTime, int count, List<TrafficLightUIState> trafficLights){



}
