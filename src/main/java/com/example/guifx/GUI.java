package com.example.guifx;

import javafx.animation.AnimationTimer;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.Group;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.scene.control.Labeled;
import javafx.scene.control.Label;

import java.util.List;

/**
 * GUI is the class that controls the JavaFX GUI
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

    @FXML
    private StackPane mapContainer;
    @FXML
    private Canvas backgroundCanvas;
    @FXML
    private Pane laneLayer;
    @FXML
    private Pane trafficLightLayer;
    @FXML
    private Pane carLayer;

    @FXML
    private Label realTimeSpeedLabel;
    @FXML
    private Label avgSpeedLabel;
    @FXML
    private Label vehicleCountLabel;
    @FXML
    private LineChart<Number, Number> speedChart;
    @FXML
    private Label statusLabel;

    /*
     * These instances manage the logic for drawing their respective layers.
     * They were ported from the Novic project.
     */

    private XYChart.Series<Number, Number> speedSeries;

    private LaneLayer laneLayerInstance;
    private CarLayer carLayerInstance;
    private TrafficLightLayer trafficLightLayerInstance;
    private AnimationTimer timer;

    private String trackedVehicleId = null;
    private Statistics statistics;

    // Zooming and Panning
    private Group zoomGroup = new Group();
    private Scale scaleTransform = new Scale(1, 1, 0, 0); // Scale from top-left (0,0)

    private double scale = 1.0;
    private final double MIN_SCALE = 0.5;
    private final double MAX_SCALE = 3.0;

    // Panning variables
    private double dragStartX, dragStartY;
    private double startTranslateX, startTranslateY;

    // Content dimensions
    private double contentWidth, contentHeight;

    /**
     * Sets the SimulationController in order to insert Cars and change the view of
     * the Sumo-GUI
     * 
     * @param simulationController
     */

    public void setSimulationController(SimulationController simulationController) {
        this.simController = simulationController;
        this.statistics = new Statistics(simController);
        // EXPLANATION: We must initialize the visual components once the controller is
        // set.
        initVisuals();

    }

    /*
     * EXPLANATION (initVisuals):
     * This helper method sets up the map scaling (MapUtil) based on the GUI size.
     * It also creates the layer instances (LaneLayer, CarLayer...) which actually
     * do the drawing work.
     * Finally, it starts the animation loop.
     */

    private void initVisuals() {
        try {
            contentWidth = mapContainer.getPrefWidth();
            contentHeight = mapContainer.getPrefHeight();

            MapUtil.setup(contentWidth, contentHeight, 15);

            laneLayerInstance = new LaneLayer(laneLayer);
            trafficLightLayerInstance = new TrafficLightLayer(trafficLightLayer, null);
            carLayerInstance = new CarLayer(carLayer);

            // chart
            speedSeries = new XYChart.Series<>();
            speedSeries.setName("Avg Speed");
            speedChart.getData().add(speedSeries);
            speedChart.setCreateSymbols(false); // for optimization

            // Move layers to zoomGroup
            mapContainer.getChildren().clear();
            zoomGroup.getChildren().addAll(backgroundCanvas, laneLayer, trafficLightLayer, carLayer);

            // Use Scale transform with pivot at (0,0) - much easier to calculate!
            zoomGroup.getTransforms().add(scaleTransform);
            mapContainer.getChildren().add(zoomGroup);

            // Clip to prevent overflow
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapContainer.widthProperty());
            clip.heightProperty().bind(mapContainer.heightProperty());
            mapContainer.setClip(clip);

            setupZoomAndDrag();

            startLoop();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to initialize visuals" + e.getMessage());

        }
    }

    private void setupZoomAndDrag() {
        mapContainer.setOnScroll(this::zoomAtMouse);

        mapContainer.setOnMousePressed(event -> {
            dragStartX = event.getSceneX();
            dragStartY = event.getSceneY();
            startTranslateX = zoomGroup.getTranslateX();
            startTranslateY = zoomGroup.getTranslateY();
        });

        mapContainer.setOnMouseDragged(event -> {
            double offsetX = event.getSceneX() - dragStartX;
            double offsetY = event.getSceneY() - dragStartY;
            zoomGroup.setTranslateX(startTranslateX + offsetX);
            zoomGroup.setTranslateY(startTranslateY + offsetY);
            clampTranslation();
        });
    }

    /**
     * Zoom at mouse position - the point under mouse stays fixed
     */
    private void zoomAtMouse(ScrollEvent event) {
        event.consume();

        double delta = event.getDeltaY();
        if (delta == 0)
            return;

        double zoomFactor = delta > 0 ? 1.1 : 0.9;
        double newScale = scale * zoomFactor;

        // Clamp scale
        if (newScale < MIN_SCALE)
            newScale = MIN_SCALE;
        if (newScale > MAX_SCALE)
            newScale = MAX_SCALE;
        if (newScale == scale)
            return;
        // Mouse position relative to mapContainer

        double mouseX = event.getX();
        double mouseY = event.getY();

        // Point in content coordinates (before scaling)
        // Since we use Scale with pivot (0,0), the math is simple:
        // screenX = contentX * scale + translateX
        // contentX = (screenX - translateX) / scale
        double contentX = (mouseX - zoomGroup.getTranslateX()) / scale;
        double contentY = (mouseY - zoomGroup.getTranslateY()) / scale;

        // Update scale
        double oldScale = scale;
        scale = newScale;
        scaleTransform.setX(scale);
        scaleTransform.setY(scale);

        // After scaling, adjust translate so the point under mouse stays fixed
        // mouseX = contentX * newScale + newTranslateX
        // newTranslateX = mouseX - contentX * newScale
        double newTranslateX = mouseX - contentX * scale;
        double newTranslateY = mouseY - contentY * scale;
        zoomGroup.setTranslateX(newTranslateX);
        zoomGroup.setTranslateY(newTranslateY);

        clampTranslation();
    }

    /**
     * Keep content within bounds - no gaps allowed
     */
    private void clampTranslation() {
        double containerWidth = mapContainer.getWidth();
        double containerHeight = mapContainer.getHeight();

        // Scaled content size
        double scaledWidth = contentWidth * scale;
        double scaledHeight = contentHeight * scale;
        double tx = zoomGroup.getTranslateX();
        double ty = zoomGroup.getTranslateY();

        // With Scale pivot at (0,0), content occupies:
        // X: [tx, tx + scaledWidth]
        // Y: [ty, ty + scaledHeight]
        if (scaledWidth <= containerWidth) {
            // Content smaller than container - center it
            tx = (containerWidth - scaledWidth) / 2;
        } else {
            // Content larger - no gaps on either side
            // Left edge (tx) should be <= 0
            if (tx > 0)
                tx = 0;
            // Right edge (tx + scaledWidth) should be >= containerWidth
            if (tx + scaledWidth < containerWidth) {
                tx = containerWidth - scaledWidth;
            }
        }

        if (scaledHeight <= containerHeight) {
            // Content smaller than container - center it
            ty = (containerHeight - scaledHeight) / 2;
        } else {
            // Content larger - no gaps on either side
            if (ty > 0)
                ty = 0;
            if (ty + scaledHeight < containerHeight) {
                ty = containerHeight - scaledHeight;
            }
        }

        zoomGroup.setTranslateX(tx);
        zoomGroup.setTranslateY(ty);
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
        // AnimationTimer timer = new AnimationTimer() {
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
     * Inserts a car and sets the view on it, when Button is clicked
     */
    // Moved it to the simulation controller
    @FXML
    public void commandSpawnVehicle(ActionEvent e) {
        System.out.println("Spawning new vehicle!");
        simController.spawnVehicle();
    }

    // Moved it to the simulation controller
    @FXML
    public void commandGetVehicleSpeed(ActionEvent e) {
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

    public void commandChangePhase(ActionEvent e) {
        System.out.println("Changing TrafficLight Phase!");
        simController.changePhase();
    }

}
