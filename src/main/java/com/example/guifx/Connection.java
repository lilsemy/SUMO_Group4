package com.example.guifx;

import de.tudresden.sumo.cmd.Simulation;
import it.polito.appeal.traci.SumoTraciConnection;


//TODO: for-each Schleife?

public class Connection {

    public static SumoTraciConnection conn;

    public static void Connection() {

        String sumo_bin = "sumo-gui"; //Gibt Variable sumo_bin den Namen von Sumo-Gui
        String config_file = "src\\main\\resources\\com\\example\\guifx\\SumoTest.sumocfg"; //Sumo Config Datei
        double step_length = 0.1;

        new Thread(() -> {
            try {
                conn = new SumoTraciConnection(sumo_bin, config_file);
                conn.addOption("step-length", step_length + "");
                conn.addOption("start", "true");

                conn.runServer();
                conn.setOrder(1);

                for (int i = 0; i < 200000; i++) {
                    conn.do_timestep();
                    double timeSeconds = (double) conn.do_job_get(Simulation.getTime());
                    System.out.println("Current Time Stamp: " + timeSeconds);
                }

                conn.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();   // <<<<< WICHTIG

    }

}
