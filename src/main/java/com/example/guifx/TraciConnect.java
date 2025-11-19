package com.example.guifx;



import it.polito.appeal.traci.SumoTraciConnection;

public class TraciConnect {

    //static SumoTraciConnection conn;
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

    //old
    //synchronized wegen thread

//    public static SumoTraciConnection getConn() throws Exception{
//        if(conn == null){
//            conn = new SumoTraciConnection(MapSumoConfig.sumo_bin, MapSumoConfig.config_file);
//            conn.addOption("step-length",String.valueOf(MapSumoConfig.step_length));
//            conn.addOption("start","true");
//            conn.runServer();
//            conn.setOrder(1);
//        }
//        return conn;
//    }

}

