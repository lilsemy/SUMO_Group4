package com.example.guifx;


import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import org.eclipse.sumo.libtraci.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Path;

/**
 * TrafficLightManager: static manager class for visualizing and controlling SUMO traffic lights.
 */
public class TrafficLightLayer {

    // Pane for traffic light nodes, injected from Controller
    private Pane trafficLightLayer;

    // Map SUMO traffic light (junction) ID to its JavaFX Circle node
    private final Map<String, Circle> trafficLightCircles = new HashMap<>();

    /**
     * Initialize traffic light layer.
     * Must be called once from Controller after FXML is loaded and MapGraphics.setup() ready
     */
    public TrafficLightLayer(Pane trafficLightLayer, Path netFilePath) {
        this.trafficLightLayer = trafficLightLayer;
        trafficLightCircles.clear();
        this.trafficLightLayer.getChildren().clear();

        // Build visual nodes for all traffic lights
        buildTrafficLightCircles();
    }

    /**
     * Build JavaFX nodes for each traffic light
     * Called once during init()
     */
    private void buildTrafficLightCircles() {
        if (trafficLightLayer == null || !MapUtil.boundsReady) {
            System.err.println("TrafficLightLayer.init() called before MapUtil was ready.");
            return;
        }

        // Get all traffic light IDs from SUMO
        List<String> trafficLightIds = TrafficLight.getIDList();
        System.out.println("Found " + trafficLightIds.size() + " traffic lights.");

        for (String trafficLightId : trafficLightIds) {
            try {
                // NOTE: Get position from JUNCTION, not TrafficLight since latter may not have a defined position
                TraCIPosition sumoPosition = Junction.getPosition(trafficLightId);
                Point2D worldPosition = new Point2D(sumoPosition.getX(), sumoPosition.getY());

                // Convert to screen coordinates
                Point2D screenPosition = MapUtil.worldToScreen(worldPosition);

                // Create a visual node circle
                Circle circle = new Circle(screenPosition.getX(), screenPosition.getY(), 2.0);
                circle.setStroke(Color.BLUE);
                circle.setStrokeWidth(1.0);
                circle.setStrokeType(StrokeType.OUTSIDE);

                // Add to scene graph and map
                trafficLightLayer.getChildren().add(circle);
                trafficLightCircles.put(trafficLightId, circle);

            } catch (Exception e) {
                System.err.println("Failed to create node for traffic light '" + trafficLightId + "': " + e.getMessage());
            }
        }

        // Do one initial update to set correct colors
        updateTrafficLightStates();
    }

    /**
     * Update the state (color) of all managed traffic light nodes.
     * Called every frame from the Controller AnimationTimer
     */
    public void updateTrafficLightStates() {
        if (trafficLightLayer == null) return;

        for (Map.Entry<String, Circle> entry : trafficLightCircles.entrySet()) {
            String trafficLightId = entry.getKey();
            Circle circle = entry.getValue();

            try {
                // Get current state string like "GGggrrrr"
                String state = TrafficLight.getRedYellowGreenState(trafficLightId);
                if (state == null || state.isEmpty()) {
                    circle.setFill(Color.GRAY); // Unknown state
                    continue;
                }

                // For simplicity, base the color on *second* signal in state string
                // Position of 2nd signal corresponds to 2nd index lane participating in the traffic light control
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
                System.err.println("Failed to update state for traffic light '" + trafficLightId + "': " + e.getMessage());
                circle.setFill(Color.BLACK); //err state
            }
        }
    }

}
