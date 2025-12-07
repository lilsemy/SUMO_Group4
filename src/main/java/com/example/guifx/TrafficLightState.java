package com.example.guifx;

/**
 * TrafficLightState is a model class for Traffic Lights
 */

public class TrafficLightState {
    private String id;
    private int duration;
    private char state;
    private int minDur;
    private int maxDur;

    public TrafficLightState(){}

    public String getId(){return id;}
    public char getState(String id){return state;}
    public int getDuration(){return duration;}
    public int getMinDur() {return minDur;}
    public int getMaxDur() {return maxDur;}




}
