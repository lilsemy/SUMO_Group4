package com.example.guifx.controller;

import com.example.guifx.util.MapUtil;
import com.example.guifx.view.LaneLayer;
import com.example.guifx.view.CarLayer;
import com.example.guifx.view.TrafficLightLayer;
import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

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

    /*
     * These instances manage the logic for drawing their respective layers.
     * They were ported from the Novic project.
     */

    private CarLayer carLayerInstance;
    private LaneLayer laneLayerInstance;
    private TrafficLightLayer trafficLightLayerInstance;
    private javafx.animation.AnimationTimer timer;




    /**
     * Sets the SimulationController in order to insert Cars and change the view of the Sumo-GUI
     * @param simulationController
    */
    
    public void setSimulationController(SimulationController simulationController){
        this.simController = simulationController;
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
            laneLayerInstance = new LaneLayer(laneLayer);
            trafficLightLayerInstance = new TrafficLightLayer(trafficLightLayer, null);
            carLayerInstance = new CarLayer(carLayer);

            startloop();

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

    private void startloop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long l) {
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


                } catch (Exception e) {
                    e.printStackTrace();
                    stop(); // to breack or stop the loop on Error
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
                System.out.println("Speed of last vehicle " + lastId + ": " + speed);
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
