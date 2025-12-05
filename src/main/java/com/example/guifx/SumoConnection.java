package com.example.guifx;

import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.StringVector;
    /**
    *SumoConnection is our object-oriented wrapper around the SUMO simulation.
    */
public class SumoConnection {

    // tracking if we have a connection or not
    private boolean connected = false;

    public SumoConnection(){}
    /**
    *Establishes new connection
    *@throws Exception
    */
    
    public void connect()throws Exception{
        if (connected){
            // Already connected, nothing to do
            return;
        }
        //To load the native libraries that libtraci needs.
        Simulation.preloadLibraries();

        StringVector args = new StringVector(new String[]{
            MapSumoConfig.sumo_bin,
            "-c",MapSumoConfig.config_file,
            "--start",
            "--step-length",String.valueOf(MapSumoConfig.step_length)});
        //,"--time-to-teleport", "-1"
        Simulation.start(args);
        connected = true;
    }
    /**
    *Does a timestep in the simulation
    *@throws Exception
    */
    public void doStep() throws Exception{
        if (!connected) {
            throw new IllegalStateException(
                    "Cannot step the simulation: not connected. " +
                            "Call connect() before doStep().");
        }
        Simulation.step();
    }
    /**
    *Closes the connection
    *@throws Exception
    */
    public void close() {
        if (!connected){
            return;
        }
        Simulation.close();
        connected = false;
    }
    /**
     * @return true if the simulation is currently running (we called connect() and not close()).
    */
    public boolean isConnected(){
            return connected;
    }
}

