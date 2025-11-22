package com.example.guifx;



import it.polito.appeal.traci.SumoTraciConnection;

    /**
    *TraciConnect is the actual Interface to SUMO
    */

public class TraciConnect {


    private SumoTraciConnection conn;
    
    public TraciConnect(){}
    /**
    *Establishes new connection
    *@throws Exception
    */
    
    public void connect()throws Exception{
        conn = new SumoTraciConnection(MapSumoConfig.sumo_bin, MapSumoConfig.config_file);
        conn.addOption("start","true");
        conn.addOption("step-length",String.valueOf(MapSumoConfig.step_length));

        conn.runServer();


    }

    /**
    *Does a timestep in the simulation
    *@throws Exception
    */
    public void doStep() throws Exception{
        conn.do_timestep();
    }

    
    /**
    *Closes the connection
    *@throws Exception
    */
    public void close()throws Exception{
        if(conn != null){
            conn.close();
        }
    }

    /**
    *Returns the Connection to SUMO
    */
    public SumoTraciConnection getConn() {
        return conn;
    }



}

