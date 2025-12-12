package com.example.guifx.view;

import com.example.guifx.util.MapUtil;
import com.example.guifx.model.TrafficLightModel;
// Import Point2D for 2D coordinates (x, y)
// Import Point2D cho tọa độ 2 chiều (x, y)
import javafx.geometry.Point2D;
// Import Pane for the layer to draw traffic lights
// Import Pane để làm lớp chứa các đèn giao thông
import javafx.scene.layout.Pane;
// Import Color for traffic light styling
// Import Color để tô màu đèn giao thông
import javafx.scene.paint.Color;
// Import Circle for drawing traffic light nodes
// Import Circle để vẽ node đèn giao thông
import javafx.scene.shape.Circle;
// Import StrokeType for circle border style
// Import StrokeType để chỉnh kiểu viền hình tròn
import javafx.scene.shape.StrokeType;
// Import SUMO TraCI classes for traffic light and junction info
// Import các lớp TraCI của SUMO để lấy thông tin đèn giao thông và nút giao
import org.eclipse.sumo.libtraci.*;

// Import MapUtil for coordinate transforms (was MapGraphics)
// Import MapUtil để chuyển đổi tọa độ (trước là MapGraphics)
//import novik.MapUtil;

// Import HashMap, List, Map for collections
// Import HashMap, List, Map để quản lý danh sách
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Import Path for file path handling
// Import Path để xử lý đường dẫn file
import java.nio.file.Path;

/**
 * TrafficLightManager: static manager class for visualizing and controlling SUMO traffic lights.
 * Lớp quản lý tĩnh để hiển thị và điều khiển đèn giao thông SUMO.
 */
public class TrafficLightLayer {

    // Pane for traffic light nodes, injected from Controller
    // Pane để chứa các node đèn giao thông, truyền từ Controller
    private Pane trafficLightLayer;

    // Map SUMO traffic light (junction) ID to its JavaFX Circle node
    // Map ID đèn giao thông (nút giao) của SUMO sang node Circle JavaFX
    private final Map<String, Circle> trafficLightCircles = new HashMap<>();

    /**
     * Initialize traffic light layer.
     * Khởi tạo lớp đèn giao thông.
     * Must be called once from Controller after FXML is loaded and MapGraphics.setup() ready
     * Phải được gọi từ Controller sau khi FXML đã load và MapGraphics.setup() đã sẵn sàng
     */
    public TrafficLightLayer(Pane trafficLightLayer, Path netFilePath) {
        this.trafficLightLayer = trafficLightLayer;
        trafficLightCircles.clear();
        this.trafficLightLayer.getChildren().clear();

        // Build visual nodes for all traffic lights
        // Tạo node hiển thị cho tất cả đèn giao thông
        buildTrafficLightCircles();
    }

    /**
     * Build JavaFX nodes for each traffic light
     * Tạo node JavaFX cho từng đèn giao thông
     * Called once during init()
     * Được gọi một lần khi khởi tạo
     */
    private void buildTrafficLightCircles() {
        if (trafficLightLayer == null || !MapUtil.boundsReady) {
            System.err.println("TrafficLightLayer.init() called before MapUtil was ready.");
            return;
        }

        // Get all traffic light IDs from SUMO
        // Lấy tất cả ID đèn giao thông từ SUMO
        List<String> trafficLightIds = TrafficLight.getIDList();
        System.out.println("Found " + trafficLightIds.size() + " traffic lights.");

        for (String trafficLightId : trafficLightIds) {
            try {
                // NOTE: Get position from JUNCTION, not TrafficLight since latter may not have a defined position
                // LƯU Ý: Lấy vị trí từ JUNCTION, không phải TrafficLight vì TrafficLight có thể không có vị trí
                TraCIPosition sumoPosition = Junction.getPosition(trafficLightId);
                Point2D worldPosition = new Point2D(sumoPosition.getX(), sumoPosition.getY());

                // Convert to screen coordinates
                // Chuyển sang tọa độ màn hình
                Point2D screenPosition = MapUtil.worldToScreen(worldPosition);

                // Create a visual node circle
                // Tạo node hình tròn hiển thị
                Circle circle = new Circle(screenPosition.getX(), screenPosition.getY(), 2.0);
                circle.setStroke(Color.BLUE);
                circle.setStrokeWidth(1.0);
                circle.setStrokeType(StrokeType.OUTSIDE);

                // Add to scene graph and map
                // Thêm vào scene graph và map
                trafficLightLayer.getChildren().add(circle);
                trafficLightCircles.put(trafficLightId, circle);

            } catch (Exception e) {
                System.err.println("Failed to create node for traffic light '" + trafficLightId + "': " + e.getMessage());
            }
        }

        // Do one initial update to set correct colors
        // Cập nhật trạng thái màu ban đầu cho các đèn giao thông
        updateTrafficLightStates();
    }

    /**
     * Update the state (color) of all managed traffic light nodes.
     * Cập nhật trạng thái (màu) cho tất cả node đèn giao thông đang quản lý.
     * Called every frame from the Controller AnimationTimer
     * Được gọi mỗi frame từ AnimationTimer trong Controller
     */
    public void updateTrafficLightStates() {
        if (trafficLightLayer == null) return;

        for (Map.Entry<String, Circle> entry : trafficLightCircles.entrySet()) {
            String trafficLightId = entry.getKey();
            Circle circle = entry.getValue();

            try {
                // Get current state string like "GGggrrrr"
                // Lấy chuỗi trạng thái hiện tại, ví dụ "GGggrrrr"
                String state = TrafficLight.getRedYellowGreenState(trafficLightId);
                if (state == null || state.isEmpty()) {
                    circle.setFill(Color.GRAY); // Unknown state
                    // Trạng thái không xác định
                    continue;
                }

                // For simplicity, base the color on *second* signal in state string
                // Đơn giản hóa: lấy màu theo ký tự thứ 2 trong chuỗi trạng thái
                // Position of 2nd signal corresponds to 2nd index lane participating in the traffic light control
                // Vị trí ký tự thứ 2 tương ứng với lane thứ 2 tham gia điều khiển đèn giao thông
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
