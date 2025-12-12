package com.example.guifx;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

import javafx.scene.control.Labeled;

import javafx.scene.control.Label;
import java.util.List;
/**
*GUI is the class that controls the JavaFX GUI
*/

public class GUI {
    private SimulationController simController;

    public GUI() throws Exception {
    }
    /*
     * EXPLANATION (New Fields):
     * These @FXML fields link to the new components added in GUI-view.fxml.
     * We replaced the TextField with a StackPane/Pane structure to draw the map.
     * - mapContainer: Holds the entire map view.
     * - laneLayer, trafficLightLayer, carLayer: Transparent Panes stacked on top of
     * each other.
     * We draw different parts of the simulation on different layers to manage them
     * easily.
     */


    @FXML private StackPane mapContainer;
    @FXML private Canvas backgroundCanvas;
    @FXML private Pane laneLayer;
    @FXML private Pane trafficLightLayer;
    @FXML private Pane carLayer;


    @FXML private Label realTimeSpeedLabel;
    @FXML private Label avgSpeedLabel;
    @FXML private Label vehicleCountLabel;

    @FXML private LineChart<Number, Number> speedChart;
    @FXML private Label statusLabel;


    /*
     * These instances manage the logic for drawing their respective layers.
     * They were ported from the Novic project.
     */
    private XYChart.Series<Number, Number> speedSeries;
    private CarLayer carLayerInstance;
    private TrafficLightLayer trafficLightLayerInstance;
    private LaneLayer laneLayerInstance;
    private AnimationTimer timer;

    private String trackedVehicleId = null;
    private Statistics statistics;

    /**
     * Sets the SimulationController in order to insert Cars and change the view of the Sumo-GUI
     * @param simulationController
    */

    public void setSimulationController(SimulationController simulationController){
        this.simController = simulationController;
        this.statistics = new Statistics(simController);
        // EXPLANATION: We must initialize the visual components once the controller is set.
        initVisuals();

    }

    /*
     * EXPLANATION (initVisuals):
     * This helper method sets up the map scaling (MapUtil) based on the GUI size.
     * It also creates the layer instances (LaneLayer, CarLayer...) which actually
     * do the drawing work.
     * Finally, it starts the animation loop.
     */

    private void initVisuals(){
        try{
            MapUtil.setup(mapContainer.getPrefWidth(),mapContainer.getPrefHeight(),15);

            LaneLayer laneLayerInstance = new LaneLayer(laneLayer);
            trafficLightLayerInstance = new TrafficLightLayer(trafficLightLayer, null);
            carLayerInstance = new CarLayer(carLayer);

            //chart
            speedSeries = new XYChart.Series<>();

            speedSeries.setName("Avg Speed");
            speedChart.getData().add(speedSeries);
            speedChart.setCreateSymbols(false); // for optimization


            startLoop();

        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println("Failed to initialize visuals"+e.getMessage());

        }
    }


    /*
     * EXPLANATION (startLoop):
     * This creates a JavaFX AnimationTimer loop.
     * The handle() method is called exactly once per generic screen frame (usually
     * 60 times/second).
     * Inside, we:
     * 1. Tell the controller to advance the physics/simulation by one step (0.05s).
     * 2. Tell the visual layers to update their positions based on the new
     * simulation state.
     * This keeps the visual representation perfectly synced with the underlying
     * simulation logic.
     */

    private void startLoop() {

        //AnimationTimer timer = new AnimationTimer() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try {
                    if (simController != null) {

                        simController.singleStep();

                        if (carLayerInstance != null) {
                            carLayerInstance.updateCars();
                        }

                        if (trafficLightLayerInstance != null) {
                            trafficLightLayerInstance.updateTrafficLightStates();
                        }
                    }
                    // statistics
                    if(statistics!=null){
                        double time = org.eclipse.sumo.libtraci.Simulation.getTime();
                        statistics.updateVehicles(time);

                        double avgSpeed = statistics.getAverageSpeed();
                        int count = simController.getVehicleController().getVehiclesMap().size();
                        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

                        avgSpeedLabel.setText("Avg Speed: " + df.format(avgSpeed)+ "ms");
                        vehicleCountLabel.setText("Vehicles: " +count);


                        speedSeries.getData().add(new XYChart.Data<>(time, avgSpeed));


                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    stop(); // stop animation on error
                }
            }
        };

        timer.start();
    }

    /**
    *Inserts a car and sets the view on it, when Button is clicked
    */
    //Moved it to the simulation controller
    @FXML
    public void commandSpawnVehicle(ActionEvent e) {
        System.out.println("Spawning new vehicle!");
        simController.spawnVehicle();
    }
    //Moved it to the simulation controller
    @FXML
    public void commandGetVehicleSpeed(ActionEvent e){
        String lastId = simController.getVehicleController().getLastVehicleId();
        if (lastId != null) {
            try {
                double speed = simController.getVehicleController().getVehicleSpeed(lastId);

                statusLabel.setText("Speed of last vehicle " + lastId + ": " + speed);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("No vehicles in simulation!");
        }
    }

    public void commandChangePhase(ActionEvent e){
        System.out.println("Changing TrafficLight Phase!");
        simController.changePhase();
    }

}
