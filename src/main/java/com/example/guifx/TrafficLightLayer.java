package com.example.guifx;


import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.sumo.libtraci.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.layout.StackPane;

/**
 * TrafficLightManager: static manager class for visualizing and controlling SUMO traffic lights.
 */
public class TrafficLightLayer {
    private static final Logger LOG = LogManager.getLogger(TrafficLightLayer.class.getName());
    private final TrafficLightController tlC;

    // Pane for traffic light nodes, injected from Controller
    private Pane trafficLightLayer;

    // Map SUMO traffic light (junction) ID to its JavaFX Circle node
    private final Map<String, Circle> trafficLightCircles = new HashMap<>();

    /**
     * Initialize traffic light layer.
     * Must be called once from Controller after FXML is loaded and MapGraphics.setup() ready
     */
    public TrafficLightLayer(Pane trafficLightLayer, Path netFilePath, TrafficLightController tlC) {
        this.trafficLightLayer = trafficLightLayer;
        trafficLightCircles.clear();
        this.trafficLightLayer.getChildren().clear();
        this.tlC = tlC;


        // Build visual nodes for all traffic lights
        buildTrafficLightCircles();
    }

    /**
     * Build JavaFX nodes for each traffic light
     * Called once during init()
     */
    private void buildTrafficLightCircles() {
        if (trafficLightLayer == null || !MapUtil.boundsReady) {
            LOG.error("TrafficLightLayer.init() called before MapUtil was ready.");
            return;
        }

        // Get all traffic light IDs from SUMO
        List<String> trafficLightIds = tlC.getTlIds();
        LOG.info("Found " + trafficLightIds.size() + " traffic lights.");

        for (String trafficLightId : trafficLightIds) {
            try {
                // NOTE: Get position from JUNCTION, not TrafficLight since latter may not have a defined position
                TraCIPosition sumoPosition = tlC.getTlPosition(tlC.getTlList().get(trafficLightId));
                Point2D worldPosition = new Point2D(sumoPosition.getX(), sumoPosition.getY());

                // Convert to screen coordinates
                Point2D screenPosition = MapUtil.worldToScreen(worldPosition);

                // Create a visual node circle
                Circle circle = new Circle(screenPosition.getX(), screenPosition.getY(), 2.0);
                circle.setStroke(Color.BLUE);
                circle.setStrokeWidth(1.0);
                circle.setStrokeType(StrokeType.OUTSIDE);

                // Create label text, to display the TL Grouping IDs
                if (trafficLightId.equals("tl2")) {
                    Text label = new Text("TL1");
                    label.setFont(Font.font(8));
                    label.setFill(Color.BLACK);
                    label.setMouseTransparent(true); // wichtig!
                    //TL Circles are now together with Text in a Pane
                    StackPane trafficLightNode = new StackPane(circle, label);
                    // Position it
                    trafficLightNode.setLayoutX(screenPosition.getX());
                    trafficLightNode.setLayoutY(screenPosition.getY());
                    // Add to scene graph
                    trafficLightLayer.getChildren().add(trafficLightNode);
                } else if (trafficLightId.equals("tl5")) {
                    Text label = new Text("TL2");
                    label.setFont(Font.font(8));
                    label.setFill(Color.BLACK);
                    label.setMouseTransparent(true); // wichtig!)
                    StackPane trafficLightNode = new StackPane(circle, label);
                    trafficLightNode.setLayoutX(screenPosition.getX());
                    trafficLightNode.setLayoutY(screenPosition.getY());
                    trafficLightLayer.getChildren().add(trafficLightNode);
                }


                // Add to scene graph and map
                trafficLightLayer.getChildren().add(circle);
                trafficLightCircles.put(trafficLightId, circle);

            } catch (Exception e) {
                LOG.error("Failed to create node for traffic light '" + trafficLightId + "': " + e.getMessage());
            }
        }

        // Do one initial update to set correct colors
        //updateTrafficLightStatesFromSnapshot();
    }

    /**
     * Update the state (color) of all managed traffic light nodes.
     * Called every frame from the Controller AnimationTimer
     */
    public void updateTrafficLightStatesFromSnapshot(List<TrafficLightUIState> lights) {
        if (trafficLightLayer == null || lights == null) return;
        // Converting List to a temporary Map for faster ID lookup during the loop
        Map<String, String> stateMap = new HashMap<>();
        for (TrafficLightUIState tl : lights) {
            stateMap.put(tl.id(), tl.state());
        }

        for (Map.Entry<String, Circle> entry : trafficLightCircles.entrySet()) {
            String trafficLightId = entry.getKey();
            Circle circle = entry.getValue();

            String state = stateMap.get(trafficLightId);
            if (state == null || state.isEmpty()) {
                circle.setFill(Color.GRAY);
                continue;
            }
            setCircleColorFromState(circle, state, trafficLightId);

//            try {
//                // Get current state string like "GGggrrrr"
//                //String state = TrafficLight.getRedYellowGreenState(trafficLightId);
//                String state = tlC.getTlList().get(trafficLightId).getRedYellowGreenState(); //--> Currently doesnt work, because states are not frequently updatet.
//                if (state == null || state.isEmpty()) {
//                    circle.setFill(Color.GRAY); // Unknown state
//                    continue;
//                }


        }
    }

    private void setCircleColorFromState(Circle circle, String state, String trafficLightId) {
        // For simplicity, base the color on *second* signal in state string
        // Position of 2nd signal corresponds to 2nd index lane participating in the traffic light control
        try {
            char secondSignal = Character.toLowerCase(state.charAt(1));


            switch (secondSignal) {
                case 'g':
                    circle.setFill(Color.GREEN);
                    break;
                case 'y':
                    circle.setFill(Color.YELLOW);
                    break;
                case 'r':
                    circle.setFill(Color.DARKRED);
                    break;
                default:
                    circle.setFill(Color.GRAY); //other
                    break;
            }

        } catch (Exception e) {
            LOG.error("Failed to update state for traffic light '" + trafficLightId + "': " + e.getMessage());
            circle.setFill(Color.BLACK); //err state
        }

    }
}

