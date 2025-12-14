package com.example.guifx;


import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import org.eclipse.sumo.libtraci.*;
import java.util.ArrayList;
import java.util.List;

public class LaneLayer {
    // Pane where we place lane shapes – similar to CarManager.layer
    private Pane laneLayer;

    // Keep references to lane shapes (optional but useful if you want to restyle/hide later)
    private final List<Polyline> lanePolylines = new ArrayList<>();

    // List of all route IDs in the network (static, for reference)
    static List<String> allRouteIds = Route.getIDList();


    /**
     * Must be called from Controller after FXML is loaded and MapGraphics.setup() has run.
     */
    LaneLayer(Pane laneLayer) {
        this.laneLayer = laneLayer;
        lanePolylines.clear();
        this.laneLayer.getChildren().clear();

        buildLanePolylines();
    }

    /**
     * Build Polyline nodes for each SUMO lane and add them to the layer.
     * This replaces the old drawNetwork() that used GraphicsContext.
     */
    private void buildLanePolylines() {
        if (laneLayer == null) {
            throw new IllegalStateException("Network layer not initialized. Call Network.init(...) first.");
        }

        // Just in case; MapUtil should already be set up in Controller
        if (!MapUtil.boundsReady) {
            // You can either throw or silently skip – here I choose to throw to catch misuse early:
            throw new IllegalStateException("MapUtil is not ready. Call MapUtil.setup(...) before Network.init().");
        }

        // Get all lane IDs from SUMO
        List<String> laneIds = Lane.getIDList();

        for (String laneId : laneIds) {
            // 1) Get shape in world coordinates
            TraCIPositionVector vec = Lane.getShape(laneId);
            List<TraCIPosition> pts = vec.getValue();
            if (pts == null || pts.isEmpty()) continue;

            // 2) Build a Polyline in *screen* coordinates
            Polyline polyline = new Polyline();

            for (TraCIPosition point : pts) {
                Point2D sumoPos = new Point2D(point.getX(), point.getY());
                Point2D javaPos = MapUtil.worldToScreen(sumoPos);

                polyline.getPoints().addAll(javaPos.getX(), javaPos.getY());
            }

            // 3) Style only (no hover/click)
            polyline.setStroke(Color.GRAY);
            polyline.setStrokeWidth(2.0);
            polyline.setFill(null);

            // 4) Add to scene graph and store reference
            laneLayer.getChildren().add(polyline);
            lanePolylines.add(polyline);
        }
    }

    // If later you want to rebuild when zoom/resize changes:
    public void rebuild() {
        if (laneLayer == null) return;
        lanePolylines.clear();
        laneLayer.getChildren().clear();
        buildLanePolylines();
    }
}
