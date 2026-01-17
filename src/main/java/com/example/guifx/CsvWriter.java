package com.example.guifx;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class CsvWriter {

    private final PrintWriter writer; // object writing text to firle

    /**
     * creating CsvWriter object -> new FileWriter(filePath) opens the file at that moment
     * If the file does not exist → it creates a new file in current working directory
     * If the file exists → it overwrites the file (by default)
     *
     * @param filePath
     * @throws IOException
     */

    public CsvWriter(String filePath) throws IOException {
        this.writer = new PrintWriter(new FileWriter(filePath));

        // Header is written exactly once here
        writer.println("time;vehicleCount;avgSpeed;congestionPresent;redLights;yellowLights;greenLights");
        writer.flush();
    }

    /**
     * Call this once every 20 steps
     * 1 real-world second = 1 / 0.05 = 20 steps
     */
    public void writeStep(double time, int vehicleCount, double avgSpeed, boolean congestionPresent, Map<String, Integer> trafficLightStates) {

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
