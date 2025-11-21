package com.example.guifx;



import it.polito.appeal.traci.SumoTraciConnection;

public class TraciConnect {


    private SumoTraciConnection conn;
    public TraciConnect(){}
    //new code
    public void connect()throws Exception{
        conn = new SumoTraciConnection(MapSumoConfig.sumo_bin, MapSumoConfig.config_file);
        conn.addOption("start","true");
        conn.addOption("step-length",String.valueOf(MapSumoConfig.step_length));

        conn.runServer();


    }

    public void doStep() throws Exception{
        conn.do_timestep();
    }

    public void close()throws Exception{
        if(conn != null){
            conn.close();
        }
    }

    public SumoTraciConnection getConn() {
        return conn;
    }



}

