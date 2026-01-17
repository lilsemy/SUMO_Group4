package com.example.guifx;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polyline;
import org.eclipse.sumo.libtraci.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.text.Text;
import javafx.scene.text.Font;

/**
 * LaneLayer is responsible for drawing lane shapes on a JavaFX Pane
 */
public class LaneLayer {
    private Pane laneLayer;
    private final List<Polyline> lanePolylines = new ArrayList<>();
    private final List<Text> laneLabels = new ArrayList<>();
    private final LaneController laneCon;

    /**
     * Constructs a LaneLayer and builds lane polylines
     * 
     * @param laneLayer Pane to draw lanes on
     */
    LaneLayer(Pane laneLayer, LaneController laneCon) {
        this.laneLayer = laneLayer;
        lanePolylines.clear();
        this.laneLayer.getChildren().clear();
        this.laneCon = laneCon;
        buildLanePolylines();
    }

    /**
     * Builds Polyline nodes for each SUMO lane + creates Labels for some Lanes for better overiew + adds them to the layer
     */
    private void buildLanePolylines() {
        if (laneLayer == null) {
            throw new IllegalStateException("Network layer not initialized. Call Network.init(...) first.");
        }

        if (!MapUtil.boundsReady) {
            throw new IllegalStateException("MapUtil is not ready. Call MapUtil.setup(...) before Network.init().");
        }

        List<String> laneIds = laneCon.getLaneIds();

        for (String laneId : laneIds) {
            TraCIPositionVector vec = laneCon.getLaneModel(laneId).getShape();
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

            //Calculate Positions of Lane Labels -> at the middle of the Lane
            int midIndex = pts.size() / 2;
            TraCIPosition midPoint = pts.get(midIndex);

            Point2D sumoMid = new Point2D(midPoint.getX(), midPoint.getY());
            Point2D javaMid = MapUtil.worldToScreen(sumoMid);

            //Only a handfull of Lanes are printed to ensure readability
            if (laneCon.getPrintLanes().contains(laneId)) {
                        Text label = new Text(laneId);
                        label.setX(javaMid.getX());
                        label.setY(javaMid.getY());
                        label.setFill(Color.DARKGRAY);
                        label.setFont(Font.font(5));

                        //Center the Text lightly
                        label.setTranslateX(2);
                        label.setTranslateY(-2);


                        laneLayer.getChildren().add(label);
                        laneLabels.add(label);

            }

        }
    }

    /**
     * Rebuilds all lane polylines, e.g., after zoom or resize
     */
    public void rebuild() {
        if (laneLayer == null) return;
        lanePolylines.clear();
        laneLabels.clear();
        laneLayer.getChildren().clear();
        buildLanePolylines();
    }
}
