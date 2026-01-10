package com.example.guifx;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class CsvWriter {

    private final PrintWriter writer; // object writing text to firle
    private boolean headerWritten = false; // tracks whether the CSV header row has already been written to avoid duplicates

    /**
     * creating CsvWriter object -> new FileWriter(filePath) opens the file at that moment
     * If the file does not exist → it creates a new file in current working directory
     * If the file exists → it overwrites the file (by default)
     *
     * @param filePath
     * @throws IOException
     */

    public CsvWriter(String filePath) throws IOException {
        this.writer = new PrintWriter(new FileWriter(filePath)); //printWriter allows for formatted writing into CSV, not just characters
    }

    /**
     * Writes the CSV header once
     */

    private void writeHeader() {
        if (!headerWritten) {
            writer.println(
                    "time;vehicleCount;avgSpeed;congestionPresent;redLights;yellowLights;greenLights"
            );
            headerWritten = true;
        }
    }

    /**
     * Call this once every 20 steps
     * 1 real-world second = 1 / 0.05 = 20 steps
     */
    public void writeStep(double time, int vehicleCount, double avgSpeed, boolean congestionPresent, Map<String, Integer> trafficLightStates) {

        writeHeader();

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

        writer.flush(); // Makes sure the text is actually written to disk immediately
    }

    public void close() {
        writer.close();
    } //Closes the file and releases system resources. Always call it after the simulation ends
    // hook this into application shutdown as a GUI close event:
    /*
    primaryStage.setOnCloseRequest(event -> {
    try {
        csv.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
});
     */
}

/*
Code for simulation steps:
Statistics stats = new Statistics(simController);
CsvWriter csv = new CsvWriter("simulation.csv");

double lastCsvWriteTime = 0.0;
int stepCount = 0;

while(simulationRunning) {

    simController.singleStep();

    double currentTime = stepCount * STEP_LENGTH;

    stats.updateVehicles(currentTime);
    stats.updateTrafficLights();

    if(currentTime - lastCsvWriteTime >= 1.0) {
        csv.writeStep(
            currentTime,
            stats.getVehicleCount(),
            stats.getAverageSpeed(),
            stats.isCongestionPresent(currentTime),
            stats.getTrafficLightStates()
        );
        lastCsvWriteTime = currentTime;
    }

    stepCount++;
}

csv.close();


 */
