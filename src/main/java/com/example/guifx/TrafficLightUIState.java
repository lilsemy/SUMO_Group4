package com.example.guifx;


/**
 * TrafficLightUIState is a Record that holds drawing data for a single traffic
 * light.
 * It is immutable and safe to pass between threads.
 */
public record TrafficLightUIState (String id, String state, double remainingTime){
}
