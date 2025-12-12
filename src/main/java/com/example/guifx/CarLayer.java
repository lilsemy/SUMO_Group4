package com.example.guifx;

// Import 2D point class from JavaFX for x, y coordinates
// Import tọa độ x y từ JavaFX
import javafx.geometry.Point2D;
// Import Pane class to contain car nodes in JavaFX
// Pane lớp chứa các node - xe JavaFX
import javafx.scene.layout.Pane;
// Import TraCIPosition to get vehicle position from SUMO via TraCI
// Lấy thông tin xe từ SUMO qua TraCI
import org.eclipse.sumo.libtraci.TraCIPosition;
// Import Vehicle class to interact with SUMO vehicles
// Lấy thông tin xe từ SUMO qua TraCI
import org.eclipse.sumo.libtraci.Vehicle;
// Import Image class for car image
// Hình ảnh đại diện cho xe
import javafx.scene.image.Image;
// Import ImageView class to display images in JavaFX
// Xử lý hình ảnh
import javafx.scene.image.ImageView;
// Import utility classes for collections and lists
// Xử lí danh sách
import java.util.*;
// Import MapUtil for coordinate transforms (was MapGraphics)
//import novik.MapUtil;


public class CarLayer {
    // Map to store cars with their ID and image view
    // Bản đồ lưu trữ các xe với ID và hình đại diện    
    private final Map<String, ImageView> carImageViews;
    // Pane to display all car nodes
    // Pane để hiển thị tất cả các node xe
    private final Pane carLayerPane;

    // The image used for all cars, loaded once
    // Hình ảnh dùng cho tất cả xe, chỉ load một lần
    private final Image carImage = loadCarImage();

    // Constructor: receives the Pane where cars will be displayed
    // Hàm khởi tạo: nhận Pane để hiển thị xe
    CarLayer(Pane carLayerPane) {

        carImageViews = new HashMap<>();
        this.carLayerPane = carLayerPane;
        // If carLayerPane is null, clear its children (defensive, but should not happen)
        // Nếu carLayerPane là null, xóa hết các node con (phòng trường hợp lỗi, nhưng thường không xảy ra)
        if (carLayerPane == null) {
            this.carLayerPane.getChildren().clear();
        }
    }
    // Load the car image from resources
    // Load hình xe từ thư mục resources
    private Image loadCarImage() {

        // problem ...
        String resourcePath = "/carblack.png";
        try {
            String absolutePath = getClass().getResource(resourcePath).toExternalForm();
            Image img = new Image(absolutePath);
            return img;
        } catch (NullPointerException e) {
            System.err.println("Failed to load image: " + resourcePath);
            return null;
        }
    }
    // Create a new car appearance and add it to the pane
    // Tạo hình xe mới và thêm vào pane
    private void createCarImageView(String carID) {
            // problem
        if (carImage  == null) {
            return;}

        ImageView carView = new ImageView(carImage);
        carView.setFitWidth(15);
        carView.setFitHeight(30);
        // Keep aspect ratio
        // Giữ tỷ lệ khung hình
        carView.setPreserveRatio(true);
        // Smooth image quality
        // Chất lượng hình ảnh mượt mà
        carView.setSmooth(true);

        carImageViews.put(carID, carView);
        carLayerPane.getChildren().add(carView);
    }

    // Update all cars: add new, update position, remove disappeared
    // Cập nhật tất cả xe: thêm mới, cập nhật vị trí, xóa xe biến mất
    public void updateCars() {
        // 1. Get the list of all vehicle IDs currently present in the SUMO simulation
        // 1. Lấy danh sách tất cả ID xe hiện đang có trong mô phỏng SUMO
        List<String> ids = Vehicle.getIDList();
        // 2. Convert the list to a set for fast lookup (active cars)
        // 2. Chuyển danh sách sang Set để tra cứu nhanh (các xe đang hoạt động)
        Set<String> activeCarIds = new HashSet<>(ids);
        // 3. Create a set of new car IDs (will remove known cars later)
        // 3. Tạo một tập hợp chứa các ID xe mới (sẽ loại bỏ các xe đã biết ở bước sau)
        Set<String> newCarIds = new HashSet<>(activeCarIds);

        // 4. Iterate through all cars currently managed (displayed)
        // 4. Duyệt qua tất cả các xe đang được quản lý (đang hiển thị trên màn hình)
        Iterator<Map.Entry<String, ImageView>> it = carImageViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ImageView> entry = it.next();
            String carId = entry.getKey();
            ImageView carView = entry.getValue();

            // 5. If the car is still active in SUMO
            // 5. Nếu xe này vẫn còn tồn tại trong mô phỏng SUMO
            if (activeCarIds.contains(carId)) {
                // Remove from newCarIds (not a new car)
                // Loại khỏi danh sách xe mới (vì đã có rồi)
                newCarIds.remove(carId);
                // Get the car's position from SUMO (TraCI)
                // Lấy vị trí xe từ SUMO (qua TraCI)
                TraCIPosition sumoPos = Vehicle.getPosition(carId, false);
                // Convert SUMO world coordinates to JavaFX screen coordinates
                // Chuyển đổi tọa độ thế giới SUMO sang tọa độ màn hình JavaFX
                Point2D javaPos = MapUtil.worldToScreen(
                        new Point2D(sumoPos.getX(), sumoPos.getY())
                );
                double w = carView.getFitWidth();
                double h = carView.getFitHeight();
                // Get the car's angle from SUMO and rotate the image accordingly
                // Lấy góc quay của xe từ SUMO và xoay hình xe cho đúng hướng
                double angle = Vehicle.getAngle(carId);
                carView.setRotate(angle);

                double radAngle = Math.toRadians(angle);
                // Calculate offset to position the car image correctly
                // Tính toán độ lệch để đặt hình xe đúng vị trí
                double offsetX = h / 2 * Math.sin(radAngle);
                double offsetY = -(h / 2) * Math.cos(radAngle);
                // Center the car image at the car's top position
                // Đặt hình xe sao cho tâm hình trùng với vị trí đầu xe
                carView.setLayoutX(javaPos.getX() - w / 2.0 - offsetX);
                carView.setLayoutY(javaPos.getY() - h / 2.0 - offsetY);
            } else {
                // 6. If the car no longer exists in SUMO, remove it from the pane and map
                // 6. Nếu xe không còn trong mô phỏng, xóa khỏi pane và khỏi carMap
                carLayerPane.getChildren().remove(carView);
                it.remove();
            }
        }

        // 7. For each new car detected in SUMO, create its appearance and add to the pane
        // 7. Với mỗi xe mới xuất hiện trong SUMO, tạo hình xe và thêm vào pane
        for (String carId : newCarIds) {
            createCarImageView(carId);
            ImageView carView = carImageViews.get(carId);

            if (carView != null) {
                // Get and convert position as above
                // Lấy và chuyển đổi vị trí như trên
                TraCIPosition sumoPosition = Vehicle.getPosition(carId, false);
                Point2D javaPosition = MapUtil.worldToScreen(
                        new Point2D(sumoPosition.getX(), sumoPosition.getY())
                );
                double width = carView.getFitWidth();
                double height = carView.getFitHeight();
                carView.setLayoutX(javaPosition.getX() - width / 2.0);
                carView.setLayoutY(javaPosition.getY() - height / 2.0);

                // Set the car's rotation to match SUMO's angle
                // Đặt góc quay của xe theo góc trong SUMO
                double angle = Vehicle.getAngle(carId);
                // The car image should be aligned horizontally with the Ox axis
                // Hình xe nên nằm ngang theo trục Ox
                carView.setRotate(angle);
            }
        }
    }
}