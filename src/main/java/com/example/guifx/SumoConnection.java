package com.example.guifx;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.sumo.libtraci.Simulation;
import org.eclipse.sumo.libtraci.StringVector;
    /**
    *SumoConnection is our object-oriented wrapper around the SUMO simulation.
    */
public class SumoConnection {

    private boolean connected = false;
    private static final Logger LOG = LogManager.getLogger(SumoConnection.class.getName());

    public SumoConnection(){}
    /**
    *Establishes new connection
    *@throws Exception
    */
    
    public void connect()throws Exception{
        if (connected){
            return;
        }

        Simulation.preloadLibraries();

        StringVector args = new StringVector(new String[]{
            MapSumoConfig.SUMO_BIN,
            "-c",MapSumoConfig.CONFIG_FILE,
            "--start",
            "--step-length",String.valueOf(MapSumoConfig.STEP_LENGTH),
                "--time-to-teleport", "-1"});

        Simulation.start(args);
        connected = true;
        LOG.info("SUMO Connection successful!");
    }
    /**
    *Does a timestep in the simulation
    *@throws Exception
    */
    public void doStep() throws Exception{
        if (!connected) {
            LOG.fatal("Cannot step the simulation: not connected. " + "Call connect() before doStep().");
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

