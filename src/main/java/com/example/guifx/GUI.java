package com.example.guifx;

import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Scale;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * GUI is the class that controls the JavaFX GUI
 */
public class GUI {
    private static final Logger LOG = LogManager.getLogger(GUI.class.getName());

    private SimulationController simController;

    @FXML
    private javafx.scene.image.ImageView stressAlarmIcon;

    private static final String ALARM_PNG = "/com/example/guifx/alarm.png";
    private static final String ALARM_GIF = "/com/example/guifx/alarm.gif";

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
    private Label Tl1Dur;
    @FXML
    private Label Tl2Dur;
    @FXML
    private LineChart<Number, Number> speedChart;

    @FXML
    private LineChart<Number, Number> travelTimeChart;

    @FXML
    private TextArea consoleArea;
    @FXML
    private ChoiceBox<String> vehicleSelector;
    @FXML
    private TextField GetDuration;
    @FXML
    private ChoiceBox<String> TLSelector;
    @FXML
    private TextField stressTestCountField;
    @FXML private Slider simSpeedSlider;

    @FXML
    private ChoiceBox<String> InsertionSelector;

    private XYChart.Series<Number, Number> speedSeries;

    private XYChart.Series<Number, Number> avgTravelTimeSeries;

    private LaneLayer laneLayerInstance;
    private CarLayer carLayerInstance;
    private TrafficLightLayer trafficLightLayerInstance;
    private AnimationTimer timer;

    private String trackedVehicleId = null;
    private Statistics statistics;

    private Group zoomGroup = new Group();
    private Scale scaleTransform = new Scale(1, 1, 0, 0);
    private Rotate rotateTransform = new Rotate(0, 0, 0);// Added rotation transform
    private boolean isFollowing = false;// Added tracking state
    private String followedVehicleId = null;// Added tracked vehicle ID
    private double scale = 1.0;
    private final double MIN_SCALE = 0.5;
    private final double MAX_SCALE = 20.0;

    private double dragStartX, dragStartY;
    private double startTranslateX, startTranslateY;

    private double contentWidth, contentHeight;

    //FILTERING vehicleFilter = The possible filters the user can choose from the choice box
    @FXML
    private ChoiceBox<TypeFilter> vehicleTypeFilter;
    //currentFilter = The applied filter that changes the GUI (For now only the statistics side)
    private TypeFilter currentTypeFilter = TypeFilter.NONE;
    @FXML
    private ChoiceBox<VehicleColor> vehicleColorFilter;
    private VehicleColor currentColorFilter = VehicleColor.NONE;

    //LOGIC FOR SPAWNING
    private SpawnConfig spawnConfig = SpawnConfig.random();
    @FXML
    private ChoiceBox<TypeFilter> typeSpawnChoice;
    @FXML
    private ChoiceBox<VehicleColor> colorSpawnChoice;



    private double remainingTime;

    // thread //
    // EXPLANATION: We replaced ScheduledExecutorService with a standard Thread.
    // Why? The executor would "queue up" tasks if the simulation got slow,
    // eventually
    // causing a massive memory leak and "freeze".
    // A simple "while(running)" loop waits passively until the previous step is
    // done,
    // so it naturally allows the CPU to breathe.
    private Thread simThread;
    // ACTION QUEUE for Thread Safety
    // EXPLANATION: The User Interface (UI) runs on one thread, and the Simulation
    // runs on another.
    // If the UI tries to "Spawn Car" directly while the Simulation is reading the
    // car list,
    // they crash (ConcurrentModificationException).
    // SOLUTION: The UI just puts a "Note" (Runnable) into this thread-safe Queue.
    // The Simulation thread picks up the notes and executes them safely when it's
    // ready.
    private final ConcurrentLinkedQueue<Runnable> actionQueue = new ConcurrentLinkedQueue<>();
    // 'volatile' ensures that changes to this variable are immediately visible to
    // other threads.
    // We use it to safely signal the background thread to stop.
    private volatile boolean running = false;
    private volatile long currentSimDelayMs = 50;
    // --- Snapshot: Letzter Zustand für UI ---
    // EXPLANATION: We cannot access SUMO or the SimulationController directly from
    // the UI thread
    // while the background thread is writing to it. This would cause "Race
    // Conditions" or crashes.
    // The "Snapshot Pattern" solves this: The background thread creates a read-only
    // copy (Snapshot)
    // of the current state. The UI thread reads only this snapshot.
    private volatile SimSnapshot latest = null;

    // chart throttle
    private static final long UI_UPDATE_NANOS = 200_000_000L; // update charts only every 200ms ;
    private long lastUiNow = 0 ;
    private static final int MAX_CHART_POINTS = 300;// limitless charts cause memory leaks and lag

    // Formatters reuse (creating new objects every frame is bad for performance)
    private final DecimalFormat df2 = new DecimalFormat("#.##");
    private final DecimalFormat df0 = new DecimalFormat("#");

    /**
     * Constructor for GUI.
     *
     * @throws Exception if initialization fails
     */
    public GUI() throws Exception {
    }

    // gui console
    public void show(String message) {
        consoleArea.appendText(message + "\n");
    }

    /**
     * Sets the SimulationController in order to insert Cars and change the view of
     * the Sumo-GUI
     *
     * @param simulationController Simulation controller instance
     */
    public void setSimulationController(SimulationController simulationController) {
        this.simController = simulationController;
        this.statistics = new Statistics(simController); //!!!!! Insert a getStatistics function im simulation Controller, beacuse we are creating there a statistic instance for CSV and thus we would have two instances of Statistics.
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

            laneLayerInstance = new LaneLayer(laneLayer, simController.getLaneController());
            trafficLightLayerInstance = new TrafficLightLayer(trafficLightLayer, null, simController.getTlController());
            carLayerInstance = new CarLayer(carLayer);
            carLayerInstance.setVehicleClickListener((vehicleId, isPinned) -> {
                if (isPinned) {
                    pinnedVehicleInfoId = vehicleId;
                    updateVehicleInfoSidebar(vehicleId);
                } else {
                    // hover only updates when nothing is pinned
                    if (pinnedVehicleInfoId == null) {
                        updateVehicleInfoSidebar(vehicleId);
                    }
                }
            });

            // clicking empty map area should clear selection ("No vehicle selected")
            // Use event filter so we don't override zoom/drag handlers.
            mapContainer.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, evt -> {
                if (evt.getTarget() == mapContainer || evt.getTarget() == backgroundCanvas) {
                    pinnedVehicleInfoId = null;
                    if (carLayerInstance != null) carLayerInstance.clearSelection();
                    updateVehicleInfoSidebar(null);
                }
            });

            // initial state
            updateVehicleInfoSidebar(null);

            speedSeries = new XYChart.Series<>();
            speedSeries.setName("Avg Speed");
            speedChart.getData().add(speedSeries);
            speedChart.setCreateSymbols(false);

            avgTravelTimeSeries = new XYChart.Series<>();
            avgTravelTimeSeries.setName("Avg Travel Time");
            travelTimeChart.getData().add(avgTravelTimeSeries);
            travelTimeChart.setCreateSymbols(false);

            mapContainer.getChildren().clear();
            zoomGroup.getChildren().addAll(backgroundCanvas, laneLayer, trafficLightLayer, carLayer);

            //zoomGroup.getTransforms().add(scaleTransform);
            zoomGroup.getTransforms().addAll(scaleTransform, rotateTransform);
            mapContainer.getChildren().add(zoomGroup);
            mapContainer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT); // Ensure Center-Right alignment

            Rectangle clip = new Rectangle();
            clip.widthProperty().bind(mapContainer.widthProperty());
            clip.heightProperty().bind(mapContainer.heightProperty());
            mapContainer.setClip(clip);

            setupZoomAndDrag();

            /**
            * Orders the vehicles in an alphabetically ascending list
            */

            vehicleSelector.setOnShowing(event -> {
                var vehicles = simController
                        .getVehicleController()
                        .getFilteredVehicleIds(currentTypeFilter, currentColorFilter);

                List<String> sortedVehicles = new ArrayList<>(vehicles);

                sortedVehicles.sort((v1, v2) -> {

                    // Prefix = everything before the number
                    String type1 = v1.replaceAll("\\d+$", "");
                    String type2 = v2.replaceAll("\\d+$", "");

                    // Extract number at the end
                    String numStr1 = v1.replaceAll("\\D+", "");
                    String numStr2 = v2.replaceAll("\\D+", "");

                    int num1 = numStr1.isEmpty() ? 0 : Integer.parseInt(numStr1);
                    int num2 = numStr2.isEmpty() ? 0 : Integer.parseInt(numStr2);

                    // Compare type alphabetically
                    int typeCompare = type1.compareTo(type2);
                    if (typeCompare != 0) return typeCompare;

                    // Compare number ASCENDING
                    return Integer.compare(num1, num2);
                });

                vehicleSelector.getItems().setAll(sortedVehicles);
            });

            vehicleSelector.getSelectionModel().selectedItemProperty().addListener((observableValue, oldValue, newValue) -> {
                if (newValue != null) {
                    followedVehicleId = newValue;
                    isFollowing = true;
                    scale = 10.0;
                    scaleTransform.setX(0);
                    scaleTransform.setY(0);
                    LOG.info("selected & Following:" + newValue);
                    show("selected & Following:" + newValue);
                }
            });

            //Initialize DropDown Menu for TrafficLights
            TLSelector.setOnShowing(event -> TLSelector.getItems().setAll("TL1", "TL2"));

            //Initialize DropDown Menu for EdgeInsertion
            //Inserting is only available for the Lanes, that are rendered on the Map
            InsertionSelector.setOnShowing(event -> {
                InsertionSelector.getItems().setAll(simController.getLaneController().getPrintLanes());
            });

            //DropDown Menu for TYPE FILTER
            vehicleTypeFilter.getItems().addAll(
                    TypeFilter.NONE,
                    TypeFilter.CAR,
                    TypeFilter.TRUCK,
                    TypeFilter.BUS
            );
            //initially the applied filter is set to NONE
            vehicleTypeFilter.setValue(TypeFilter.NONE);

            //updating the applied filter after user input
            vehicleTypeFilter
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observableValue, oldValue, newValue) -> {
                        if (newValue != null) {
                            currentTypeFilter = newValue;
                        }
                    });

            //Drop down menu for COLOR FILTER
            vehicleColorFilter.getItems().addAll(
                    VehicleColor.NONE,
                    VehicleColor.RED,
                    VehicleColor.BLACK,
                    VehicleColor.WHITE,
                    VehicleColor.YELLOW
            );
            //Color filter is initially set to NONE
            vehicleColorFilter.setValue(VehicleColor.NONE);
            //updating the color filter
            vehicleColorFilter
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observableValue, oldValue, newValue) -> {
                        if (newValue != null) {
                            currentColorFilter = newValue;
                        }
                    });

            // Listen to Slider to adjust delay dynamically
            // Left (0) = Very Slow (2000ms delay)
            // Middle (50) = Normal (100ms delay)
            // Right (100) = Very Fast (0ms delay)
            simSpeedSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
                double val = newValue.doubleValue();
                if(val >= 50) {
                    // Range 50 to 100 -> Mapping to 100ms down to 0ms
                    // Formula: (100 - val) * 2
                    // Ex: 50 -> 50*2 = 100ms. 100 -> 0*2 = 0ms.
                    currentSimDelayMs = (long) ((100 - val)*2);


                } else {
                    // Range 0 to 50 -> Mapping to 2000ms down to 100ms
                    // Formula: 100 + (50 - val) * 40
                    // Ex: 50 -> 100 + 0 = 100ms. 0 -> 100 + 50*40 = 2100ms.
                    currentSimDelayMs = (long) (100 +(50 - val)*40);
                }
            });


            typeSpawnChoice.getItems().addAll(
                    TypeFilter.NONE,
                    TypeFilter.CAR,
                    TypeFilter.TRUCK,
                    TypeFilter.BUS
            );

            typeSpawnChoice.setValue(TypeFilter.NONE);

            typeSpawnChoice
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observableValue, oldValue, newValue) -> {
                        if(newValue != null) {
                            updateSpawnConfig(typeSpawnChoice.getValue(), colorSpawnChoice.getValue());
                        }
                    });

            colorSpawnChoice.getItems().addAll(
                    VehicleColor.NONE,
                    VehicleColor.WHITE,
                    VehicleColor.BLACK,
                    VehicleColor.RED,
                    VehicleColor.YELLOW
            );

            colorSpawnChoice.setValue(VehicleColor.NONE);

            colorSpawnChoice
                    .getSelectionModel()
                    .selectedItemProperty()
                    .addListener((observableValue, oldValue, newValue) -> {
                        if(newValue != null) {
                            updateSpawnConfig(typeSpawnChoice.getValue(), colorSpawnChoice.getValue());
                        }
                    });

            startLoop();

        } catch (Exception e) {
            e.printStackTrace();
            LOG.fatal("Failed to initialize visuals" + e.getMessage());
            show("Failed to initialize visuals" + e.getMessage());
        }
    }

    /**
     * Sets up zoom and drag handlers for the map container
     */
    private void setupZoomAndDrag() {
        mapContainer.setFocusTraversable(true);
        mapContainer.setOnKeyPressed(event -> {
            boolean rotate = false;
            double dAngle = 0;
            switch (event.getCode()) {
                case Q:
                    dAngle = -3;
                    rotate = true;
                    break;
                case E:
                    dAngle = 5;
                    rotate = true;
                    break;
                default:
                    break;
            }
            if (rotate) {
                if (isFollowing) {
                    rotateTransform.setAngle(rotateTransform.getAngle() + dAngle);
                } else {
                    double cx = mapContainer.getWidth() / 2;
                    double cy = mapContainer.getHeight() / 2;

                    try {
                        Point2D localCenter = zoomGroup.parentToLocal(cx, cy);
                        rotateTransform.setAngle(rotateTransform.getAngle() + dAngle);
                        Point2D newParentPos = zoomGroup.localToParent(localCenter);
                        double dx = newParentPos.getX() - cx;
                        double dy = newParentPos.getY() - cy;

                        zoomGroup.setTranslateX(zoomGroup.getTranslateX() - dx);
                        zoomGroup.setTranslateY(zoomGroup.getTranslateY() - dy);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        mapContainer.setOnScroll(this::zoomAtMouse);

        mapContainer.setOnMousePressed(event -> {
            if (isFollowing) {
                isFollowing = false;//stop
            }
            mapContainer.requestFocus();

            try {
                Point2D localPoint = zoomGroup.parentToLocal(event.getX(), event.getY());
                rotateTransform.setAngle(0);
                rotateTransform.setPivotX(0);
                rotateTransform.setPivotY(0);
                scaleTransform.setPivotX(0);
                scaleTransform.setPivotY(0);

                double currentScale = scaleTransform.getX();
                zoomGroup.setTranslateX(event.getX() - zoomGroup.getLayoutX() - localPoint.getX() * currentScale);
                zoomGroup.setTranslateY(event.getY() - zoomGroup.getLayoutY() - localPoint.getY() * currentScale);
            } catch (Exception e) {

            }
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

        if (isFollowing) {
            scale = newScale;
            scaleTransform.setX(scale);
            scaleTransform.setY(scale);
            return;
        }
        double mouseX = event.getX();
        double mouseY = event.getY();

        try {
            Point2D localPoint = zoomGroup.parentToLocal(mouseX, mouseY);
            rotateTransform.setAngle(0);
            rotateTransform.setPivotX(0);
            rotateTransform.setPivotY(0);
            scaleTransform.setPivotX(0);
            scaleTransform.setPivotY(0);
            scale = newScale;
            scaleTransform.setX(scale);
            scaleTransform.setY(scale);
            zoomGroup.setTranslateX(mouseX - zoomGroup.getLayoutX() - localPoint.getX() * scale);
            zoomGroup.setTranslateY(mouseY - zoomGroup.getLayoutY() - localPoint.getY() * scale);
        } catch (Exception e) {
            scale = newScale;
            scaleTransform.setX(scale);
            scaleTransform.setY(scale);
        }


        /*double contentX = (mouseX - zoomGroup.getTranslateX()) / scale;
        double contentY = (mouseY - zoomGroup.getTranslateY()) / scale;

        double oldScale = scale;
        scale = newScale;
        scaleTransform.setX(scale);
        scaleTransform.setY(scale);

        double newTranslateX = mouseX - contentX * scale;
        double newTranslateY = mouseY - contentY * scale;
        zoomGroup.setTranslateX(newTranslateX);
        zoomGroup.setTranslateY(newTranslateY);*/

        clampTranslation();
    }

    /**
     * Clamps translation to prevent gaps when panning
     */
    private void clampTranslation() {
       /* double containerWidth = mapContainer.getWidth();
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
        zoomGroup.setTranslateY(ty);*/
    }

    /**
     * Starts the animation loop that updates simulation and visuals
     */
    private void startLoop() {
        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                try{
                if (!running){
                    stop();
                    return;
                }
                SimSnapshot snap = latest;
                if ( snap == null){
                    return;
                }
                // 1) Cars zeichnen: snapshot nutzen
                if (carLayerInstance != null) {
                            //carLayerInstance.updateCarsFromSnapshot(simController.getVehicleController().getFilteredVehicleIds(currentTypeFilter, currentColorFilter));
                    carLayerInstance.updateCarsFromSnapshot(snap.vehicles());
                        }
                // 2) Traffic lights: Draw traffic lights from Snapshot
                if (trafficLightLayerInstance != null) {
                            trafficLightLayerInstance.updateTrafficLightStatesFromSnapshot(snap.trafficLights());
                }

                // 3) Follow/Rotation:
                if (isFollowing && followedVehicleId != null) {
                    try {
                        VehicleUiState targetVs = null;
                        for(VehicleUiState vs : snap.vehicles()){
                            if ( vs.id().equals(followedVehicleId)){
                                targetVs = vs;
                                break;
                            }
                        //if (simController.getVehicleController().getVehicle(followedVehicleId) == null) {

                        }
                        if (targetVs != null){
                                    //var sumoPos = Vehicle.getPosition(followedVehicleId, false);
                                    //double angle = Vehicle.getAngle(followedVehicleId);

                                    Point2D worldPos = new Point2D(targetVs.x(), targetVs.y());
                                    Point2D targetLocalPos = MapUtil.worldToScreen(worldPos);
                                    double targetAngleVal = targetVs.angle();
                                    double smoothFactor = 0.1;
                                    double currentAngle = rotateTransform.getAngle();
                                    double nextAngle = currentAngle + (-targetAngleVal - currentAngle) * smoothFactor;

                                    rotateTransform.setAngle(nextAngle);
                                    rotateTransform.setPivotX(targetLocalPos.getX());
                                    rotateTransform.setPivotY(targetLocalPos.getY());
                                    scaleTransform.setPivotX(targetLocalPos.getX());
                                    scaleTransform.setPivotY(targetLocalPos.getY());

                                    double containerW = mapContainer.getWidth();
                                    double containerH = mapContainer.getHeight();

                                    double targetTx = (containerW / 2.0) - targetLocalPos.getX();
                                    double targetTy = (containerH / 2.0) - targetLocalPos.getY();

                                    double currrentTx = zoomGroup.getTranslateX();
                                    double currrentTy = zoomGroup.getTranslateY();
                                    zoomGroup.setTranslateX(currrentTx + (targetTx - currrentTx) * smoothFactor);
                                    zoomGroup.setTranslateY(currrentTy + (targetTy - currrentTy) * smoothFactor);
                                }
                    } catch (Exception e) {

                    }
                }
                // 4) Labels + Chart nur alle 200ms (throttled)

                    if (now - lastUiNow >= UI_UPDATE_NANOS){
                        lastUiNow = now;



                    //
                    //double time = org.eclipse.sumo.libtraci.Simulation.getTime();

//                    if (statistics != null) {
//                        statistics.updateVehicles(time);
//                      /*
//                        double avgSpeed = statistics.getAverageSpeed();
//                        int count = simController.getVehicleController().getVehiclesMap().size();*/
//                        Collection<VehicleModel> filteredVehiclesList =
//                                simController
//                                        .getVehicleController()
//                                        .getFilteredVehicles(currentTypeFilter, currentColorFilter);
//                        double avgSpeed = statistics.getAverageSpeed(filteredVehiclesList);
//                        double avgTravelTime = statistics.updateAndGetAverageTravelTime(time);
//                        int count = filteredVehiclesList.size();

                        //java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");

                        avgSpeedLabel.setText("Avg Speed: " + df0.format(snap.avgSpeed()) + "ms");
                        vehicleCountLabel.setText("Vehicles: " + snap.count());
                        speedSeries.getData().add(new XYChart.Data<>(snap.time(), snap.avgSpeed()));
                        avgTravelTimeSeries.getData().add(new XYChart.Data<>(snap.time(), snap.avgTravelTime()));

                        //Traffic Light 1/2 Remaining time..
                        double tl1Rem = 0, tl2Rem = 0;
                        for ( TrafficLightUIState tl : snap.trafficLights()){
                            if(tl.id().equals("tl1")){
                                tl1Rem = tl.remainingTime();
                            }
                            if( tl.id().equals("tl4")){
                                tl2Rem = tl.remainingTime();
                            }
                        }
                        Tl1Dur.setText("Traffic Light 1: " + df0.format(tl1Rem) + "s");
                        Tl2Dur.setText("Traffic Light 2: " + df0.format(tl2Rem) + "s");
                        // Performance: Cap the number of points in charts to prevent lag
                        if (speedSeries.getData().size() > MAX_CHART_POINTS) {
                            speedSeries.getData().remove(0, speedSeries.getData().size() - MAX_CHART_POINTS);
                        }
                        if (avgTravelTimeSeries.getData().size() > MAX_CHART_POINTS) {
                            avgTravelTimeSeries.getData().remove(0,
                                    avgTravelTimeSeries.getData().size() - MAX_CHART_POINTS);
                        }

                    }

//                    //TrafficLight Durations
//                    java.text.DecimalFormat df = new java.text.DecimalFormat("#");
//                    remainingTime = simController.getTlController().remainingTime("tl1") - time;
//                    Tl1Dur.setText("Traffic Light 1: " + df.format(remainingTime) + "s");
//                    remainingTime = simController.getTlController().remainingTime("tl4") - time;
//                    Tl2Dur.setText("Traffic Light 2: " + df.format(remainingTime) + "s");
//                    //synchronize TrafficLightModels with TL values in Simualtion
//                    simController.getTlController().updateTLModel();

                } catch (Exception e) {
                    LOG.fatal("Error in Simulation Loop: " + e.getMessage());
                    e.printStackTrace();
                    //stop();
                }
            }
        };
        startSimulationThread();
        timer.start();
    }

    public void updateSpawnConfig(TypeFilter selectedType, VehicleColor selectedColor){

        if(selectedType == TypeFilter.NONE && selectedColor == VehicleColor.NONE){
            spawnConfig = SpawnConfig.random();
        }

        if(selectedType == TypeFilter.NONE && selectedColor != VehicleColor.NONE){
            spawnConfig = SpawnConfig.restrictColors(EnumSet.of(selectedColor));
        }

        if(selectedType != TypeFilter.NONE && selectedColor == VehicleColor.NONE){
            spawnConfig = SpawnConfig.restrictTypes(EnumSet.of(selectedType));
        }

        if(selectedType != TypeFilter.NONE && selectedColor != VehicleColor.NONE){
            spawnConfig = SpawnConfig.restrictAll(EnumSet.of(selectedType), EnumSet.of(selectedColor));
        }


    }

    /**
     * Inserts a car and sets the view on it
     *
     * @param e ActionEvent from button click
     */

    public void commandSpawnVehicle(ActionEvent e) {
        LOG.info("Spawning new vehicle!");
        show("Spawning new vehicle!");
        actionQueue.add(()-> {
        //String id = simController.spawnVehicle(spawnConfig);
        //if (id != null) {
         //   System.out.println("Spawned:" + id);
         //   javafx.application.Platform.runLater(()-> log("Spawned:" + id));
        if (InsertionSelector.getValue() == null) {
            String id = simController.spawnVehicle(spawnConfig, null);
            if (id != null) {
                LOG.info("Spawned:" + id);
                javafx.application.Platform.runLater(()-> show("Spawned:" + id + " randomly!"));
            }
        }
        else {
            String id = simController.spawnVehicle(spawnConfig, InsertionSelector.getValue());
            if (id != null) {
                LOG.info("Spawned:" + id + " on Lane: " + InsertionSelector.getValue() + "!");
                javafx.application.Platform.runLater(()-> show("Spawned:" + id + " on Lane: " + InsertionSelector.getValue() + "!"));
            }
        }
    });
    }


    public void commandFollowVehicle(ActionEvent e) {
        String selectedId = null;
        String targetId = null;
        String lastId = null;
        lastId = simController.getVehicleController().getLastVehicleId();
        selectedId = vehicleSelector.getValue();

        if (selectedId != null) {
            targetId = selectedId;
        } else if (lastId != null) {
            targetId = lastId;
        } else {
            targetId = lastId;
        }
        if (targetId == null) {
            LOG.info("no vehicle selected!");
            show("no vehicle  selected!");
        }


        followedVehicleId = targetId;
        isFollowing = true;

        scale = 10.0;
        scaleTransform.setX(scale);
        scaleTransform.setY(scale);

        LOG.info("Following:" + targetId);
        show("Following:" + targetId);
    }

    /**
     * Gets the speed of the last vehicle
     *
     * @param e ActionEvent from button click
     */

    public void commandGetVehicleSpeed(ActionEvent e) {
        actionQueue.add(() -> {
            String lastId = simController.getVehicleController().getLastVehicleId();
            if (lastId != null) {
                try {
                    double speed = simController.getVehicleController().getVehicleSpeed(lastId);
                    //java.text.DecimalFormat dv = new java.text.DecimalFormat("#.##");
                    LOG.info("Speed of last vehicle " + lastId + ": " + df2.format(speed));
                    javafx.application.Platform.runLater(() -> show("Speed of last vehicle " + lastId + ": " + df2.format(speed)));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                LOG.warn("No vehicles in simulation!");
                javafx.application.Platform.runLater(() -> show("No vehicles in simulation!"));
            }
        });
    }

    /**
     * Changes the traffic light phase
     *
     * @param e ActionEvent from button click
     */
    public void commandChangePhase(ActionEvent e) {
        if (TLSelector.getValue() != null){

            if (!GetDuration.getText().isEmpty()) {
                try {
                    double newDur = Double.parseDouble(GetDuration.getText());
                    String tl = TLSelector.getValue();
                    actionQueue.add(() -> {
                        simController.changePhase(newDur, tl);
                        LOG.info("Changing TrafficLight Phase of Traffic Light: " + tl + "with Phase Duration of: " + newDur + " seconds!");
                        show("Changing TrafficLight Phase of Traffic Light: " + tl + "with Phase Duration of: " + newDur + " seconds!");
                    });
                } catch (Exception ex) {
                    LOG.error("Changing TrafficLight states failed");
                    show("Changing TrafficLight states failed");
                }
            }    else {
                LOG.error("Error! Please enter a valid number for the Phase duration!");
                show("Error! Please enter a valid number for the Phase duration!");
            }
        }
        else {
            LOG.error("Please select a Traffic Light");
        }
    }

    /**
     * Starts a stress test with x amount of vehicles
     *
     * @param e ActionEvent from button click
     */

    public void commandStressTest(ActionEvent e) {
        try {
            int count = Integer.parseInt(stressTestCountField.getText());

            if (count <= 0) {
                javafx.application.Platform.runLater(() -> show("Please enter a positive number for stress test."));

                // ensure icon is reset
                setStressAlarmIcon(false);
                return;
            }

            if (count > 100) {
                LOG.error("Maximum allowed vehicles for stress test is 100.");
                javafx.application.Platform.runLater(() ->
                        show("Maximum allowed vehicles for stress test is 100.")
                );
                setStressAlarmIcon(false);
                return;
            }

            LOG.info("Starting Stress Test with " + count + " vehicles");
            show("Starting Stress Test with " + count + " vehicles");

            simController.startStressTest(count, spawnConfig);

            // Trigger 10s alarm blink + gif icon
            startStressAlarmBlink(Duration.seconds(10));

        } catch (NumberFormatException ex) {
            show("Invalid number. Please enter a valid integer.");
            LOG.error("Invalid stress test number");
            setStressAlarmIcon(false);
        }
    }

    private void setStressAlarmIcon(boolean active) {
        if (stressAlarmIcon == null) return;

        String res = active ? ALARM_GIF : ALARM_PNG;
        try {
            var url = getClass().getResource(res);
            if (url != null) {
                stressAlarmIcon.setImage(new Image(url.toExternalForm()));
            }
        } catch (Exception ignored) {
        }
    }

    // Alarm (10s blink) for stress test
    private Timeline alarmBlinkTimeline;
    private Timeline alarmAutoStopTimeline;
    private boolean alarmBlinkOn = false;

    /**
     * Starts a red blink alarm on the whole UI for a fixed duration.
     * This only changes a CSS class on the root, so it won't affect the map container styling.
     */
    private void startStressAlarmBlink(Duration totalDuration) {
        stopStressAlarmBlink();

        javafx.scene.Parent root = (mapContainer != null && mapContainer.getScene() != null)
                ? mapContainer.getScene().getRoot()
                : null;
        if (root == null) {
            return;
        }

        setStressAlarmIcon(true);
        alarmBlinkOn = false;

        alarmBlinkTimeline = new Timeline(
                new KeyFrame(Duration.millis(200), evt -> {
                    if (alarmBlinkOn) {
                        root.getStyleClass().remove("alarm-blink");
                    } else {
                        if (!root.getStyleClass().contains("alarm-blink")) {
                            root.getStyleClass().add("alarm-blink");
                        }
                    }
                    alarmBlinkOn = !alarmBlinkOn;
                })
        );
        alarmBlinkTimeline.setCycleCount(Timeline.INDEFINITE);
        alarmBlinkTimeline.play();

        alarmAutoStopTimeline = new Timeline(new KeyFrame(totalDuration, evt -> stopStressAlarmBlink()));
        alarmAutoStopTimeline.setCycleCount(1);
        alarmAutoStopTimeline.play();
    }

    private void stopStressAlarmBlink() {
        javafx.scene.Parent root = (mapContainer != null && mapContainer.getScene() != null)
                ? mapContainer.getScene().getRoot()
                : null;

        if (alarmBlinkTimeline != null) {
            alarmBlinkTimeline.stop();
            alarmBlinkTimeline = null;
        }
        if (alarmAutoStopTimeline != null) {
            alarmAutoStopTimeline.stop();
            alarmAutoStopTimeline = null;
        }

        alarmBlinkOn = false;
        setStressAlarmIcon(false);

        if (root != null) {
            root.getStyleClass().remove("alarm-blink");
        }
    }


    public void commandPrintCongestion(ActionEvent e) {
        actionQueue.add(() -> {
            double time = org.eclipse.sumo.libtraci.Simulation.getTime();

            Map<String, Double> congestions =
                    statistics.detectCongestionHotspots(time);

            if (congestions.isEmpty()) {
                javafx.application.Platform.runLater(() -> show("No congestion found"));
                LOG.info("No congestion found");
                return;
            }

            javafx.application.Platform.runLater(() -> show("Congestion detected:"));
            LOG.info("Congestion detected:");

            for (Map.Entry<String, Double> entry : congestions.entrySet()) {

                String message =
                        "Lane " + entry.getKey() +
                                " → avg speed: " +
                                String.format("%.2f", entry.getValue()) +
                                " m/s";

                javafx.application.Platform.runLater(() -> show(message));
                LOG.info(message);
            }
        });
    }

    // Right sidebar vehicle info
    @FXML private javafx.scene.control.Label vehicleInfoHeader;
    @FXML private javafx.scene.control.Label vehicleInfoId;
    @FXML private javafx.scene.control.Label vehicleInfoType;
    @FXML private javafx.scene.control.Label vehicleInfoColor;
    @FXML private javafx.scene.control.Label vehicleInfoSpeed;
    @FXML private javafx.scene.control.Label vehicleInfoLane;
    @FXML private javafx.scene.control.Label vehicleInfoRoute;
    @FXML private javafx.scene.control.Label vehicleInfoPos;
    @FXML private javafx.scene.control.Label vehicleInfoAngle;

    private String pinnedVehicleInfoId;
    private String hoveredVehicleInfoId;

    private void updateVehicleInfoSidebar(String vehicleId) {
        // sidebar might not exist in some older FXMLs
        if (vehicleInfoHeader == null) return;

        // keep track of hover vs pinned to allow real-time refresh
        if (Objects.equals(vehicleId, pinnedVehicleInfoId)) {
            // pinned is tracked separately
        } else {
            hoveredVehicleInfoId = vehicleId;
        }

        if (vehicleId == null || simController == null) {
            vehicleInfoHeader.setText("No vehicle selected");
            if (vehicleInfoId != null) vehicleInfoId.setText("ID: -");
            if (vehicleInfoType != null) vehicleInfoType.setText("Type: -");
            if (vehicleInfoColor != null) vehicleInfoColor.setText("Color: -");
            if (vehicleInfoSpeed != null) vehicleInfoSpeed.setText("Speed: -");
            if (vehicleInfoLane != null) vehicleInfoLane.setText("Lane: -");
            if (vehicleInfoRoute != null) vehicleInfoRoute.setText("Route: -");
            if (vehicleInfoPos != null) vehicleInfoPos.setText("Pos: -");
            if (vehicleInfoAngle != null) vehicleInfoAngle.setText("Angle: -");
            return;
        }

        VehicleModel v = simController.getVehicleController().getVehicle(vehicleId);
        if (v == null) {
            updateVehicleInfoSidebar(null);
            return;
        }

        java.text.DecimalFormat df2 = new java.text.DecimalFormat("#.##");
        java.text.DecimalFormat df0 = new java.text.DecimalFormat("#");

        vehicleInfoHeader.setText("Selected: " + vehicleId);
        if (vehicleInfoId != null) vehicleInfoId.setText("ID: " + v.getId());
        if (vehicleInfoType != null) vehicleInfoType.setText("Type: " + safe(v.getTypeId()));
        if (vehicleInfoColor != null) vehicleInfoColor.setText("Color: " + String.valueOf(v.getColor()));
        if (vehicleInfoSpeed != null) vehicleInfoSpeed.setText("Speed: " + df2.format(v.getSpeed()) + " m/s");
        if (vehicleInfoLane != null) vehicleInfoLane.setText("Lane: " + safe(v.getLaneId()));
        if (vehicleInfoRoute != null) vehicleInfoRoute.setText("Route: " + safe(v.getRouteId()));
        if (vehicleInfoPos != null) vehicleInfoPos.setText("Pos: " + df2.format(v.getX()) + ", " + df2.format(v.getY()));
        if (vehicleInfoAngle != null) vehicleInfoAngle.setText("Angle: " + df0.format(v.getAngle()));
    }

    private static String safe(String s) {
        return s == null ? "-" : s;
    }


    // Neue Methode zum Starten des Sim-Threads (Self-Throttling Loop)
    private void startSimulationThread() {
        running = true;
        simThread = new Thread(() -> {
            // CsvWriter initialization here

            CsvWriter csv = null;
            try {
               csv =  new CsvWriter("simulation.csv");
            }catch (Exception e) {
                e.printStackTrace();
            }
            double lastCsvWriteTime = 0.0;


            while (running) {
                try {
                    long startTime = System.currentTimeMillis();
                    Runnable task;
                    while ((task = actionQueue.poll()) != null) {
                        try {
                            task.run();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                    }


                    simController.singleStep();
                    double time = simController.getTime();


                    Collection<String> ids = simController.getVehicleController().getFilteredVehicleIds(currentTypeFilter, currentColorFilter);

                    List<VehicleUiState> states = new ArrayList<>(Math.max(1, ids.size()));

                    double sumSpeed = 0;
                    int validSpeedCount = 0;
                    for (String id : ids) {
                        try {
                            VehicleModel v = simController.getVehicleController().getVehicle(id);
                            if (v != null) {
                                states.add(new VehicleUiState(id, v.getX(), v.getY(), v.getAngle(), v.getTypeId(), v.getColor()));

                                // Calculate Stats "on the fly"
                                sumSpeed += v.getSpeed();
                                validSpeedCount++;
                            }
                        } catch (Exception ex) {

                        }
                    }
                    double avg = (validSpeedCount > 0) ?  sumSpeed / validSpeedCount :0.0;
                    int count = states.size();
                    double avgTrvelTime = 0;
                    if (statistics != null) {
                        statistics.updateVehicles(time);
                        avgTrvelTime = statistics.updateAndGetAverageTravelTime(time);
                        statistics.updateTrafficLights();
                    }
                    long stepDuration = System.currentTimeMillis() - startTime;

                    // If step took significantly longer than allowed (e.g. > currentDelay + 20ms
                    // buffer)
                    // and we are not in "Unlimited/Turbo" mode (delay != 0)
                    if (currentSimDelayMs > 0 && stepDuration > currentSimDelayMs + 50) {
                        LOG.info("System Overload: Step took " + stepDuration + "ms (Target: "
                                + currentSimDelayMs + "ms)");
                    }

                    simController.getTlController().updateTLModel();

                    List<TrafficLightUIState> tlStates = new ArrayList<>();
                    for (var entry : simController.getTlController().getTlList().entrySet()) {
                        String id = entry.getKey();
                        String state = entry.getValue().getRedYellowGreenState();
                        double rem = simController.getTlController().remainingTime(id) - time;
                        tlStates.add(new TrafficLightUIState(id, state, Math.max(0, rem)));
                    }
                    latest = new SimSnapshot(time, states, avg, avgTrvelTime, count, tlStates);


                    // csv
                    if( csv != null && time - lastCsvWriteTime >= 1.0) {
                        csv.writeStep(
                                time,
                                statistics.getVehicleCount(),
                                statistics.getAverageSpeed(),
                                statistics.isCongestionPresent(time),
                                statistics.getTrafficLightStates()
                        );
                        lastCsvWriteTime = time;

                    }

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    long wait = currentSimDelayMs - elapsedTime;
                    if (wait > 0) {
                        Thread.sleep(wait);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (csv != null) {
                csv.close();
            }

        });
        simThread.setDaemon(true);
        simThread.start();
    }
    public void stopAll(){
        running = false;
        if(timer != null){
            timer.stop();
        }
        if(simController != null){
            simController.stopSimulation();

        }
        if(simController != null && simController.getConnection() != null){
            simController.getConnection().close();
        }
    }



}

