package com.example.guifx;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class CsvWriter {

    private final PrintWriter writer;

    /**
     * creating CsvWriter object -> new FileWriter(fileName)
     * If the file does not exist → it creates a new file in current working directory
     * If the file exists → it overwrites the file (by default)
     * PrintWriter allows for formatted writing
     *
     * @param fileName
     * @throws IOException
     */

    public CsvWriter(String fileName) throws IOException {
        this.writer = new PrintWriter(new FileWriter(fileName));

        writer.println("time;vehicleCount;avgSpeed;congestionPresent;redLights;yellowLights;greenLights");
        writer.flush();
    }

    /**
     * Writes a single row of simulation data to the CSV file.
     * Each call records the current simulation time, vehicle statistics,
     * congestion status, and the number of traffic lights in each state.
     *
     * @param time the current simulation time
     * @param vehicleCount the number of active vehicles
     * @param avgSpeed the average speed of all vehicles
     * @param congestionPresent whether congestion is currently detected
     * @param trafficLightStates a map containing counts of traffic lights by state ("R", "Y", "G")
     */
    public void writeStep(double time, int vehicleCount, double avgSpeed, boolean congestionPresent, Map<String, Integer> trafficLightStates) {

        try {
            int r = trafficLightStates.getOrDefault("R", 0);
            int y = trafficLightStates.getOrDefault("Y", 0);
            int g = trafficLightStates.getOrDefault("G", 0);

            writer.printf(
                    "%.2f;%d;%.3f;%b;%d;%d;%d%n",
                    time,
                    vehicleCount,
                    avgSpeed,
                    congestionPresent,
                    r, y, g
            );
        } catch (Exception e) {

            System.err.println("Error writing CSV step at time " + time + ": " + e.getMessage());
        }
    }

    /**
     * Closes the CSV file.
     * Must be called once when the simulation ends.
     */

   public void close() {
        writer.flush();
        writer.close();
   }
}
