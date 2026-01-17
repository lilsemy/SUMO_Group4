package com.example.guifx;

    /**
    *MapSumoConfig is holding the Configuration Files for SUMO Simulation
    */

public class MapSumoConfig {
    private MapSumoConfig(){ } // no default constructor .. not changeable

    public static final String SUMO_BIN = "sumo";
    // The SUMO binary (can be "sumo" or "sumo-gui")
    public static final String CONFIG_FILE = "src/main/resources/com/example/guifx/SumoConfig/complex.sumocfg";
    //This file defines the network, routes, and simulation parameters.
    public static final double STEP_LENGTH = 0.05;
    //time step in seconds (0.1 means 10 steps per second)

}
