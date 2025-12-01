package novik;

// Import new concise class names for panel update
// Import các class mới đã đổi tên cho cập nhật panel
import novik.CarLayer;
import novik.TrafficLightLayer;
import novik.LaneLayer;

public class InfoPanel {
    // Đã bỏ toàn bộ chức năng panel đèn giao thông, giữ lại class rỗng để tránh lỗi tham chiếu nếu còn gọi tới
    public InfoPanel() {}
    public void updatePanel(CarLayer carLayer, TrafficLightLayer trafficLightLayer, LaneLayer laneLayer) {
        // Không làm gì cả
    }
}

