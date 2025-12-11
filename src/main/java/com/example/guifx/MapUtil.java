package com.example.guifx;

// Import Point2D for 2D coordinates (x, y)
// Import Point2D cho tọa độ 2 chiều (x, y)
import javafx.geometry.Point2D;
// Import SUMO TraCI classes for lane and position info
// Import các lớp TraCI của SUMO để lấy thông tin lane và vị trí
import org.eclipse.sumo.libtraci.Lane;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.TraCIPositionVector;

// Import ArrayList and List for collections
// Import ArrayList và List để quản lý danh sách
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;

public class MapUtil {
    // List of all lane polylines in world coordinates
    // Danh sách tất cả polyline lane theo tọa độ thế giới
    private static final List<List<Point2D>> lanePolylinesWorld = new ArrayList<>();
    // World bounds and scale
    // Biên và tỉ lệ của thế giới
    private static double minX, maxX, minY, maxY;
    private static double scale; // from meters in SUMO to pixels in JavaFX
    // từ mét trong SUMO sang pixel trong JavaFX
    private static double canvasWidth, canvasHeight, MARGIN;
    public static boolean boundsReady =false; // true if bounds/scale are set
    // true nếu đã tính được biên/tỉ lệ

    // Setup graphics: set canvas size, margin, load network, compute bounds
    // Khởi tạo đồ họa: đặt kích thước canvas, margin, load mạng lưới, tính biên
    public static void setup(double _canvasWidth, double _canvasHeight, double _MARGIN){
        canvasWidth = _canvasWidth;
        canvasHeight = _canvasHeight;
        MARGIN = _MARGIN;
        loadNetworkGeometry();
        computeWorldBounds();
    }

    // Load all lane shapes from SUMO network
    // Load tất cả hình dạng lane từ mạng lưới SUMO
    private static void loadNetworkGeometry(){
        lanePolylinesWorld.clear();

        List<String> landIds = Lane.getIDList();
        for (String landId : landIds){
            // get the shape of each lane (lanes are different from edges)
            // Lấy hình dạng của từng lane (lane khác edge)
            // In our JAR, getShape(String) returns TraCIPositionVector
            // Trong JAR này, getShape(String) trả về TraCIPositionVector
            TraCIPositionVector vec = Lane.getShape(landId);

            // convert to list of points of coordination
            // Chuyển sang danh sách các điểm tọa độ
            // vec.getValue() -> java.util.List<TraCIPosition>
            List<TraCIPosition> pts = vec.getValue();

            if (pts == null || pts.isEmpty()) continue;

            List<Point2D> lanePolyline = new ArrayList<>(pts.size());
            for (TraCIPosition lanePoint: pts){
                lanePolyline.add(new Point2D(lanePoint.getX(), lanePoint.getY()));
            }
            lanePolylinesWorld.add(lanePolyline);
        }
    }

    // Compute the world bounds and scale to fit the canvas
    // Tính toán biên của thế giới và tỉ lệ để vừa canvas
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

        // Fit-to-canvas scale (preserve aspect, flip Y later in mapping)
        // Tính tỉ lệ để vừa canvas (giữ tỉ lệ, sẽ lật trục Y khi vẽ)
        double worldW = Math.max(1e-6, (maxX - minX));
        double worldH = Math.max(1e-6, (maxY - minY));
        double usableW = Math.max(1, canvasWidth - 2*MARGIN);
        double usableH = Math.max(1, canvasHeight - 2*MARGIN);
        scale = Math.min(usableW/worldW, usableH/worldH);
        boundsReady = true;
    }

    // Convert a point from world (SUMO) coordinates to screen (JavaFX) coordinates
    // Chuyển một điểm từ tọa độ thế giới (SUMO) sang tọa độ màn hình (JavaFX)
    public static Point2D worldToScreen(Point2D point){
        // Translate to origin, scale, flip Y to match JavaFX-downwards
        // Tịnh tiến về gốc, nhân tỉ lệ, lật trục Y cho đúng hướng JavaFX
        double x = (point.getX() - minX) * scale;
        double y = (point.getY() - minY) * scale;

        double sx = MARGIN + x;
        double sy = canvasHeight - MARGIN - y;
        return new Point2D(sx,sy);
    }
}


