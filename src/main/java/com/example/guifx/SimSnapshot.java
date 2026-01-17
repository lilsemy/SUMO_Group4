package com.example.guifx;
import java.util.List;



/**
 * SimSnapshot is a Record that holds ALL data required to render one frame on
 * the screen.
 * It is created by the Simulation Thread and read by the UI Thread
 * (AnimationTimer).
 * Records are immutable and thread-safe by default.
 */

public record SimSnapshot (double time, List <VehicleUiState> vehicles, double avgSpeed,double avgTravelTime, int count, List<TrafficLightUIState> trafficLights){



}
