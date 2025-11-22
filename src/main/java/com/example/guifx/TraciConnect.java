package com.example.guifx;



import it.polito.appeal.traci.SumoTraciConnection;

    /**
    *TraciConnect is a
    */

public class TraciConnect {


    private SumoTraciConnection conn;
    
    public TraciConnect(){}
    //new code
    /**
    *Establishes new connection
    *@throws
    */
    
    public void connect()throws Exception{
        conn = new SumoTraciConnection(MapSumoConfig.sumo_bin, MapSumoConfig.config_file);
        conn.addOption("start","true");
        conn.addOption("step-length",String.valueOf(MapSumoConfig.step_length));

        conn.runServer();


    }

    /**
    *Does a timestep in the simulation
    *@throws
    */
    public void doStep() throws Exception{
        conn.do_timestep();
    }

    
    /**
    *Closes the connection
    *@throws
    */
    public void close()throws Exception{
        if(conn != null){
            conn.close();
        }
    }

    /**
    *
    */
    public SumoTraciConnection getConn() {
        return conn;
    }



}

