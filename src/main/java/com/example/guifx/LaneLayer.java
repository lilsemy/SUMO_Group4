package com.example.guifx;

// Import Point2D for 2D coordinates (x, y)
// Import Point2D cho tọa độ 2 chiều (x, y)
import javafx.geometry.Point2D;
// Import Pane for the layer to draw lanes
// Import Pane để làm lớp chứa các lane
import javafx.scene.layout.Pane;
// Import Color for lane styling
// Import Color để tô màu lane
import javafx.scene.paint.Color;
// Import Polyline for drawing lane shapes
// Import Polyline để vẽ hình dạng lane
import javafx.scene.shape.Polyline;
// Import SUMO TraCI classes for lane and route info
// Import các lớp TraCI của SUMO để lấy thông tin lane và route
import org.eclipse.sumo.libtraci.*;
// Import MapUtil for coordinate transforms (was MapGraphics)
// Import novik.MapUtil để chuyển đổi tọa độ (trước đây là MapGraphics)
//import novik.MapUtil;

// Import ArrayList and List for collections
// Import ArrayList và List để quản lý danh sách
import java.util.ArrayList;
import java.util.List;

public class LaneLayer {
    // Pane where we place lane shapes – similar to CarManager.layer
    // Pane để chứa các lane (giống như CarManager.layer)
    private Pane laneLayer;

    // Keep references to lane shapes (optional but useful if you want to restyle/hide later)
    // Lưu tham chiếu tới các Polyline lane (có thể dùng để đổi style hoặc ẩn/hiện về sau)
    private final List<Polyline> lanePolylines = new ArrayList<>();

    // List of all route IDs in the network (static, for reference)
    // Danh sách tất cả route ID trong mạng lưới (static, chỉ để tham khảo)
    static List<String> allRouteIds = Route.getIDList();


    /**
     * Must be called from Controller after FXML is loaded and MapGraphics.setup() has run.
     * Hàm này phải được gọi từ Controller sau khi FXML đã load và MapGraphics.setup() đã chạy.
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
     * Tạo các node Polyline cho từng lane của SUMO và thêm vào layer.
     * Thay thế cho hàm drawNetwork() cũ dùng GraphicsContext.
     */
    private void buildLanePolylines() {
        if (laneLayer == null) {
            throw new IllegalStateException("Network layer not initialized. Call Network.init(...) first.");
        }

        // Just in case; MapUtil should already be set up in Controller
        // Chỉ để chắc chắn; MapUtil nên đã được setup trong Controller
        if (!MapUtil.boundsReady) {
            // You can either throw or silently skip – here I choose to throw to catch misuse early:
            // Có thể throw hoặc bỏ qua – ở đây throw để phát hiện lỗi sớm
            throw new IllegalStateException("MapUtil is not ready. Call MapUtil.setup(...) before Network.init().");
        }

        // Get all lane IDs from SUMO
        // Lấy tất cả lane ID từ SUMO
        List<String> laneIds = Lane.getIDList();

        for (String laneId : laneIds) {
            // 1) Get shape in world coordinates
            // 1) Lấy hình dạng lane theo tọa độ thế giới (SUMO)
            TraCIPositionVector vec = Lane.getShape(laneId);
            List<TraCIPosition> pts = vec.getValue();
            if (pts == null || pts.isEmpty()) continue;

            // 2) Build a Polyline in *screen* coordinates
            // 2) Tạo Polyline theo tọa độ màn hình JavaFX
            Polyline polyline = new Polyline();

            for (TraCIPosition point : pts) {
                Point2D sumoPos = new Point2D(point.getX(), point.getY());
                Point2D javaPos = MapUtil.worldToScreen(sumoPos);

                polyline.getPoints().addAll(javaPos.getX(), javaPos.getY());
            }

            // 3) Style only (no hover/click)
            // 3) Chỉ style (không hover/click)
            polyline.setStroke(Color.GRAY);
            polyline.setStrokeWidth(2.0);
            polyline.setFill(null);

            // 4) Add to scene graph and store reference
            // 4) Thêm vào scene graph và lưu lại tham chiếu
            laneLayer.getChildren().add(polyline);
            lanePolylines.add(polyline);
        }
    }

    // If later you want to rebuild when zoom/resize changes:
    // Nếu sau này muốn vẽ lại khi zoom/thay đổi kích thước:
    public void rebuild() {
        if (laneLayer == null) return;
        lanePolylines.clear();
        laneLayer.getChildren().clear();
        buildLanePolylines();
    }
}

