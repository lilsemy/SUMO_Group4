package com.example.guifx;

    /**
    *MapSumoConfig is a
    */

public class MapSumoConfig {
    private MapSumoConfig(){ } // no defaul constructor .. not changeable

    public static final String sumo_bin = "sumo-gui"; // The SUMO binary (can be "sumo" or "sumo-gui")
    public static final String config_file = "src/main/resources/com/example/guifx/SumoTest.sumocfg";//This file defines the network, routes, and simulation parameters.
    public static final double step_length = 0.1; //time step in seconds (0.1 means 10 steps per second)




}
