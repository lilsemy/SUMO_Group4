package com.example.guifx;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import org.eclipse.sumo.libtraci.*;
import java.util.ArrayList;
import java.util.List;

/**
 * LaneLayer is responsible for drawing lane shapes on a JavaFX Pane
 */
public class LaneLayer {
    // Pane where we place lane shapes
    private Pane laneLayer;

    // Keep references to lane shapes
    private final List<Polyline> lanePolylines = new ArrayList<>();

    // List of all route IDs in the network
    static List<String> allRouteIds = Route.getIDList();

    /**
     * Constructs a LaneLayer and builds lane polylines
     * 
     * @param laneLayer Pane to draw lanes on
     */
    LaneLayer(Pane laneLayer) {
        this.laneLayer = laneLayer;
        lanePolylines.clear();
        this.laneLayer.getChildren().clear();

        buildLanePolylines();
    }

    /**
     * Builds Polyline nodes for each SUMO lane and adds them to the layer
     */
    private void buildLanePolylines() {
        if (laneLayer == null) {
            throw new IllegalStateException("Network layer not initialized. Call Network.init(...) first.");
        }

        if (!MapUtil.boundsReady) {
            throw new IllegalStateException("MapUtil is not ready. Call MapUtil.setup(...) before Network.init().");
        }

        List<String> laneIds = Lane.getIDList();

        for (String laneId : laneIds) {
            TraCIPositionVector vec = Lane.getShape(laneId);
            List<TraCIPosition> pts = vec.getValue();
            if (pts == null || pts.isEmpty()) continue;

            Polyline polyline = new Polyline();

            for (TraCIPosition point : pts) {
                Point2D sumoPos = new Point2D(point.getX(), point.getY());
                Point2D javaPos = MapUtil.worldToScreen(sumoPos);

                polyline.getPoints().addAll(javaPos.getX(), javaPos.getY());
            }

            polyline.setStroke(Color.GRAY);
            polyline.setStrokeWidth(2.0);
            polyline.setFill(null);

            laneLayer.getChildren().add(polyline);
            lanePolylines.add(polyline);
        }
    }

    /**
     * Rebuilds all lane polylines, e.g., after zoom or resize
     */
    public void rebuild() {
        if (laneLayer == null) return;
        lanePolylines.clear();
        laneLayer.getChildren().clear();
        buildLanePolylines();
    }
}
