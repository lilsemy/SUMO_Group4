package com.example.guifx;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Group;
import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.util.Duration;

/** GUIController handles all GUI logic and animations */
public class GUIController {
    private final GUI view;
    private final SimulationController simController;

    /*
     * These instances manage the logic for drawing their respective layers.
     * They were ported from the Novic project.
     */
    private LaneLayer laneLayerInstance;
    private CarLayer carLayerInstance;
    private TrafficLightLayer trafficLightLayerInstance;
    private Statistics statistics;
    private XYChart.Series<Number, Number> speedSeries;

    // Alarm Button
    private boolean alarmActive = false;
    private boolean isRed = false;
    // Animation for alarm button
    private Timeline alarmTimeline;

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

    public GUIController(GUI view, SimulationController simController) {
        this.view = view;
        this.simController = simController;
        this.statistics = new Statistics(simController);
    }

    /*
     * EXPLANATION (initVisuals):
     * This helper method sets up the map scaling (MapUtil) based on the GUI size.
     * It also creates the layer instances (LaneLayer, CarLayer...) which actually
     * do the drawing work.
     * Finally, it starts the animation loop.
     */
    public void init() {
        try {
            contentWidth = view.getMapContainer().getPrefWidth();
            contentHeight = view.getMapContainer().getPrefHeight();

            // Fallback sizes
            if (contentWidth <= 0)
                contentWidth = 800;
            if (contentHeight <= 0)
                contentHeight = 600;

            // Setup canvas
            view.getBackgroundCanvas().setWidth(contentWidth);
            view.getBackgroundCanvas().setHeight(contentHeight);
            var gc = view.getBackgroundCanvas().getGraphicsContext2D();
            gc.setFill(javafx.scene.paint.Color.web("#1a1a1a"));
            gc.fillRect(0, 0, contentWidth, contentHeight);

            // Setup layers
            view.getLaneLayer().setPrefSize(contentWidth, contentHeight);
            view.getTrafficLightLayer().setPrefSize(contentWidth, contentHeight);
            view.getCarLayer().setPrefSize(contentWidth, contentHeight);

            MapUtil.setup(contentWidth, contentHeight, 15);

            laneLayerInstance = new LaneLayer(view.getLaneLayer());
            trafficLightLayerInstance = new TrafficLightLayer(view.getTrafficLightLayer(), null);
            carLayerInstance = new CarLayer(view.getCarLayer());

            // Setup zoom group
            view.getMapContainer().getChildren().clear();
            zoomGroup.getChildren().clear();
            zoomGroup
                    .getChildren()
                    .addAll(
                            view.getBackgroundCanvas(),
                            view.getLaneLayer(),
                            view.getTrafficLightLayer(),
                            view.getCarLayer());
            zoomGroup.getTransforms().add(scaleTransform);
            view.getMapContainer().getChildren().add(zoomGroup);

            // Clip
            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(view.getMapContainer().widthProperty());
            clip.heightProperty().bind(view.getMapContainer().heightProperty());
            view.getMapContainer().setClip(clip);

            setupZoomAndDrag();
            setupChart();
            startLoop();

            if (view.getStatusLabel() != null)
                view.getStatusLabel().setText("Simulation started");

            // Set initial alarm icon
            if (view.getAlarmIcon() != null) {
                view.getAlarmIcon().setImage(new Image(getClass().getResourceAsStream("/com/example/guifx/alarm.png")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to initialize visuals: " + e.getMessage());
        }
    }

    public void toggleAlarm() {
        if (!alarmActive) {
            view.getAlarmIcon().setImage(new Image(getClass().getResourceAsStream("/com/example/guifx/alarm.gif")));
            alarmActive = true;
            view.getAlarmButton().setText("Stop Stress Test");

            // Get vehicle count from TextField
            int count = 0;
            try {
                String text = view.getAlarmVehicleCountField().getText();
                count = Integer.parseInt(text);
                if (count <= 0)
                    throw new NumberFormatException();
            } catch (Exception ex) {
                view.getStatusLabel().setText("add a valid vehicle number!");
                alarmActive = false;
                view.getAlarmButton().setText("Start Stress Test");
                view.getAlarmIcon().setImage(new Image(getClass().getResourceAsStream("/com/example/guifx/alarm.png")));
                return;
            }

            simController.startStressTest(count); // call stress test logic in controller
            // Animation for alarm button - value blink is =)))
            alarmTimeline = new Timeline(new KeyFrame(Duration.seconds(0.20040108), evt -> toggleAlarmColor()));
            alarmTimeline.setCycleCount(Timeline.INDEFINITE);
            alarmTimeline.play();
        } else {
            view.getAlarmIcon().setImage(new Image(getClass().getResourceAsStream("/com/example/guifx/alarm.png")));
            alarmActive = false;
            view.getAlarmButton().setText("Start Stress Test");
            simController.stopStressTest(); // stop stress test logic in controller
            // Animation for alarm button
            if (alarmTimeline != null) {
                alarmTimeline.stop();
            }
            // Reset alarm blink effect
            view.getRootPane().getStyleClass().remove("alarm-blink");
        }
    }

    // Alarm color toggle
    private void toggleAlarmColor() {
        if (isRed) {
            view.getRootPane().getStyleClass().remove("alarm-blink");
        } else {
            view.getRootPane().getStyleClass().add("alarm-blink");
        }
        isRed = !isRed;
    }

    private void setupChart() {
        speedSeries = new XYChart.Series<>();
        speedSeries.setName("Avg Speed");
        view.getSpeedChart().getData().clear();
        view.getSpeedChart().getData().add(speedSeries);
        view.getSpeedChart().setCreateSymbols(false);
    }

    private void setupZoomAndDrag() {
        view.getMapContainer().setOnScroll(this::zoomAtMouse);

        view.getMapContainer()
                .setOnMousePressed(
                        event -> {
                            dragStartX = event.getSceneX();
                            dragStartY = event.getSceneY();
                            startTranslateX = zoomGroup.getTranslateX();
                            startTranslateY = zoomGroup.getTranslateY();
                        });

        view.getMapContainer()
                .setOnMouseDragged(
                        event -> {
                            double offsetX = event.getSceneX() - dragStartX;
                            double offsetY = event.getSceneY() - dragStartY;
                            zoomGroup.setTranslateX(startTranslateX + offsetX);
                            zoomGroup.setTranslateY(startTranslateY + offsetY);
                            clampTranslation();
                        });
    }

    /** Zoom at mouse position - the point under mouse stays fixed */
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
        scale = newScale;
        scaleTransform.setX(scale);
        scaleTransform.setY(scale);
        // After scaling, adjust translate so the point under mouse stays fixed
        double newTranslateX = mouseX - contentX * scale;
        double newTranslateY = mouseY - contentY * scale;
        zoomGroup.setTranslateX(newTranslateX);
        zoomGroup.setTranslateY(newTranslateY);
        clampTranslation();
        updateZoomLabel();
    }

    private void updateZoomLabel() {
        if (view.getZoomLabel() != null) {
            view.getZoomLabel().setText(String.format("Zoom: %.0f%%", scale * 100));
        }
    }

    /** Keep content within bounds - no gaps allowed */
    private void clampTranslation() {
        double containerWidth = view.getMapContainer().getWidth();
        double containerHeight = view.getMapContainer().getHeight();
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
        AnimationTimer timer = new AnimationTimer() {
            private int frameCount = 0;

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

                        // Update stats every 10 frames
                        frameCount++;
                        if (frameCount >= 10) {
                            frameCount = 0;
                            updateStatistics();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    stop();
                }
            }
        };
        timer.start();
    }

    private void updateStatistics() {
        try {
            double simTime = org.eclipse.sumo.libtraci.Simulation.getTime();

            if (statistics != null) {
                statistics.updateVehicles(simTime);
            }

            int vehicleCount = statistics.getCurrentVehiclesSize();
            double avgSpeed = statistics.getAverageSpeed();

            view.getVehicleCountLabel().setText(String.valueOf(vehicleCount));
            view.getAvgSpeedLabel().setText(String.format("%.1f m/s", avgSpeed));
            view.getSimTimeLabel().setText(String.format("%.1fs", simTime));

            speedSeries.getData().add(new XYChart.Data<>(simTime, avgSpeed));
        } catch (Exception e) {
            // Ignore if SUMO not ready
        }
    }

    /** Inserts a car and sets the view on it, when Button is clicked */
    public void spawnVehicle() {
        simController.spawnVehicle();
        view.getStatusLabel().setText("Vehicle spawned");
    }

    public void getLastVehicleSpeed() {
        String lastId = simController.getVehicleController().getLastVehicleId();
        if (lastId != null) {
            try {
                double speed = simController.getVehicleController().getVehicleSpeed(lastId);
                view.getStatusLabel().setText(String.format("Vehicle %s: %.1f m/s", lastId, speed));
            } catch (Exception ex) {
                view.getStatusLabel().setText("Error getting speed");
            }
        } else {
            view.getStatusLabel().setText("No vehicles in simulation");
        }
    }

    public void changePhase() {
        simController.changePhase();
        view.getStatusLabel().setText("Traffic light phase changed");
    }
}