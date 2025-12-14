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
import javafx.scene.control.Label;

/**
 * GUI is the class that controls the JavaFX GUI
 */
public class GUI {
    private SimulationController simController;

    /**
     * Constructor for GUI.
     *
     * @throws Exception if initialization fails
     */
    public GUI() throws Exception {
    }

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

    private XYChart.Series<Number, Number> speedSeries;

    private LaneLayer laneLayerInstance;
    private CarLayer carLayerInstance;
    private TrafficLightLayer trafficLightLayerInstance;
    private AnimationTimer timer;

    private String trackedVehicleId = null;
    private Statistics statistics;

    private Group zoomGroup = new Group();
    private Scale scaleTransform = new Scale(1, 1, 0, 0);

    private double scale = 1.0;
    private final double MIN_SCALE = 0.5;
    private final double MAX_SCALE = 3.0;

    private double dragStartX, dragStartY;
    private double startTranslateX, startTranslateY;

    private double contentWidth, contentHeight;

    /**
     * Sets the SimulationController in order to insert Cars and change the view of
     * the Sumo-GUI
     * 
     * @param simulationController Simulation controller instance
     */
    public void setSimulationController(SimulationController simulationController) {
        this.simController = simulationController;
        this.statistics = new Statistics(simController);
        initVisuals();
    }

    /**
     * Initializes the visual layers and starts the animation loop
     */
    private void initVisuals() {
        try {
            contentWidth = mapContainer.getPrefWidth();
            contentHeight = mapContainer.getPrefHeight();

            MapUtil.setup(contentWidth, contentHeight, 15);

            laneLayerInstance = new LaneLayer(laneLayer);
            trafficLightLayerInstance = new TrafficLightLayer(trafficLightLayer, null);
            carLayerInstance = new CarLayer(carLayer);

            speedSeries = new XYChart.Series<>();
            speedSeries.setName("Avg Speed");
            speedChart.getData().add(speedSeries);
            speedChart.setCreateSymbols(false);

            mapContainer.getChildren().clear();
            zoomGroup.getChildren().addAll(backgroundCanvas, laneLayer, trafficLightLayer, carLayer);

            zoomGroup.getTransforms().add(scaleTransform);
            mapContainer.getChildren().add(zoomGroup);

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

    /**
     * Sets up zoom and drag handlers for the map container
     */
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
     * Zooms the map at the mouse pointer
     * 
     * @param event Scroll event
     */
    private void zoomAtMouse(ScrollEvent event) {
        event.consume();

        double delta = event.getDeltaY();
        if (delta == 0)
            return;

        double zoomFactor = delta > 0 ? 1.1 : 0.9;
        double newScale = scale * zoomFactor;

        if (newScale < MIN_SCALE)
            newScale = MIN_SCALE;
        if (newScale > MAX_SCALE)
            newScale = MAX_SCALE;
        if (newScale == scale)
            return;

        double mouseX = event.getX();
        double mouseY = event.getY();

        double contentX = (mouseX - zoomGroup.getTranslateX()) / scale;
        double contentY = (mouseY - zoomGroup.getTranslateY()) / scale;

        double oldScale = scale;
        scale = newScale;
        scaleTransform.setX(scale);
        scaleTransform.setY(scale);

        double newTranslateX = mouseX - contentX * scale;
        double newTranslateY = mouseY - contentY * scale;
        zoomGroup.setTranslateX(newTranslateX);
        zoomGroup.setTranslateY(newTranslateY);

        clampTranslation();
    }

    /**
     * Clamps translation to prevent gaps when panning
     */
    private void clampTranslation() {
        double containerWidth = mapContainer.getWidth();
        double containerHeight = mapContainer.getHeight();

        double scaledWidth = contentWidth * scale;
        double scaledHeight = contentHeight * scale;
        double tx = zoomGroup.getTranslateX();
        double ty = zoomGroup.getTranslateY();

        if (scaledWidth <= containerWidth) {
            tx = (containerWidth - scaledWidth) / 2;
        } else {
            if (tx > 0)
                tx = 0;
            if (tx + scaledWidth < containerWidth) {
                tx = containerWidth - scaledWidth;
            }
        }

        if (scaledHeight <= containerHeight) {
            ty = (containerHeight - scaledHeight) / 2;
        } else {
            if (ty > 0)
                ty = 0;
            if (ty + scaledHeight < containerHeight) {
                ty = containerHeight - scaledHeight;
            }
        }

        zoomGroup.setTranslateX(tx);
        zoomGroup.setTranslateY(ty);
    }

    /**
     * Starts the animation loop that updates simulation and visuals
     */
    private void startLoop() {
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

                    if (statistics != null) {
                        double time = org.eclipse.sumo.libtraci.Simulation.getTime();
                        statistics.updateVehicles(time);

                        double avgSpeed = statistics.getAverageSpeed();
                        int count = simController.getVehicleController().getVehiclesMap().size();
                        java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

                        avgSpeedLabel.setText("Avg Speed: " + df.format(avgSpeed) + "ms");
                        vehicleCountLabel.setText("Vehicles: " + count);
                        speedSeries.getData().add(new XYChart.Data<>(time, avgSpeed));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    stop();
                }
            }
        };

        timer.start();
    }

    /**
     * Inserts a car and sets the view on it
     * 
     * @param e ActionEvent from button click
     */
    @FXML
    public void commandSpawnVehicle(ActionEvent e) {
        System.out.println("Spawning new vehicle!");
        simController.spawnVehicle();
    }

    /**
     * Gets the speed of the last vehicle
     * 
     * @param e ActionEvent from button click
     */
    @FXML
    public void commandGetVehicleSpeed(ActionEvent e) {
        String lastId = simController.getVehicleController().getLastVehicleId();
        if (lastId != null) {
            try {
                double speed = simController.getVehicleController().getVehicleSpeed(lastId);
                java.text.DecimalFormat dv = new java.text.DecimalFormat("#.##");
                statusLabel.setText("Speed of last vehicle " + lastId + ": " + dv.format(speed));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            System.out.println("No vehicles in simulation!");
        }
    }

    /**
     * Changes the traffic light phase
     * 
     * @param e ActionEvent from button click
     */
    public void commandChangePhase(ActionEvent e) {
        System.out.println("Changing TrafficLight Phase!");
        simController.changePhase();
    }

    /**
     * Starts a stress test with 50 vehicles
     * 
     * @param e ActionEvent from button click
     */
    public void commandStressTest(ActionEvent e){
        System.out.println("Starting Stress Test");
        statusLabel.setText("Starting Stress Test");
        simController.startStressTest(50);
    }
}
