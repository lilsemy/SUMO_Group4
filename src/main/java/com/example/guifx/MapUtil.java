package com.example.guifx;

import javafx.geometry.Point2D;
import org.eclipse.sumo.libtraci.Lane;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.TraCIPositionVector;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

/**
 * MapUtil provides utility methods to convert SUMO world coordinates to JavaFX screen coordinates
 */
public class MapUtil {
    // List of all lane polylines in world coordinates
    private static final List<List<Point2D>> lanePolylinesWorld = new ArrayList<>();
    // World bounds and scale
    private static double minX, maxX, minY, maxY;
    private static double scale; // from meters in SUMO to pixels in JavaFX
    private static double canvasWidth, canvasHeight, MARGIN;
    public static boolean boundsReady = false; // true if bounds/scale are set

    /**
     * Sets up the map graphics, computes bounds and scale
     * 
     * @param _canvasWidth width of the JavaFX canvas
     * @param _canvasHeight height of the JavaFX canvas
     * @param _MARGIN margin around map
     */
    public static void setup(double _canvasWidth, double _canvasHeight, double _MARGIN){
        canvasWidth = _canvasWidth;
        canvasHeight = _canvasHeight;
        MARGIN = _MARGIN;
        loadNetworkGeometry();
        computeWorldBounds();
    }

    /**
     * Loads lane shapes from SUMO network
     */
    private static void loadNetworkGeometry(){
        lanePolylinesWorld.clear();

        List<String> landIds = Lane.getIDList();
        for (String landId : landIds){
            TraCIPositionVector vec = Lane.getShape(landId);
            List<TraCIPosition> pts = vec.getValue();
            if (pts == null || pts.isEmpty()) continue;

            List<Point2D> lanePolyline = new ArrayList<>(pts.size());
            for (TraCIPosition lanePoint: pts){
                lanePolyline.add(new Point2D(lanePoint.getX(), lanePoint.getY()));
            }
            lanePolylinesWorld.add(lanePolyline);
        }
    }

    /**
     * Computes world bounds and scale to fit the canvas
     */
    private static void computeWorldBounds(){
        DoubleSummaryStatistics xs = new DoubleSummaryStatistics();
        DoubleSummaryStatistics ys = new DoubleSummaryStatistics();

        for (List<Point2D> poly : lanePolylinesWorld){
            for (Point2D point : poly){
                xs.accept(point.getX());
                ys.accept(point.getY());
            }
        }

        if (xs.getCount() == 0) {
            throw new IllegalStateException("Blank map");
        } else {
            minX = xs.getMin();
            maxX = xs.getMax();
            minY = ys.getMin();
            maxY = ys.getMax();
        }

        double worldW = Math.max(1e-6, (maxX - minX));
        double worldH = Math.max(1e-6, (maxY - minY));
        double usableW = Math.max(1, canvasWidth - 2*MARGIN);
        double usableH = Math.max(1, canvasHeight - 2*MARGIN);
        scale = Math.min(usableW/worldW, usableH/worldH);
        boundsReady = true;
    }

    /**
     * Converts a point from SUMO world coordinates to JavaFX screen coordinates
     * 
     * @param point world coordinates
     * @return screen coordinates
     */
    public static Point2D worldToScreen(Point2D point){
        double x = (point.getX() - minX) * scale;
        double y = (point.getY() - minY) * scale;

        double sx = MARGIN + x;
        double sy = canvasHeight - MARGIN - y;
        return new Point2D(sx, sy);
    }
}
