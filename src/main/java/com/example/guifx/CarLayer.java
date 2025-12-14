package com.example.guifx;

// Import 2D point class from JavaFX for x, y coordinates
// Import tọa độ x y từ JavaFX
import java.util.*;
import javafx.geometry.Point2D;
// Import Pane class to contain car nodes in JavaFX
// Pane lớp chứa các node - xe JavaFX
import javafx.scene.image.Image;
// Import ImageView class to display images in JavaFX
// Xử lý hình ảnh
import javafx.scene.image.ImageView;
// Import utility classes for collections and lists
// Xử lí danh sách
import javafx.scene.layout.Pane;
// Import TraCIPosition to get vehicle position from SUMO via TraCI
// Lấy thông tin xe từ SUMO qua TraCI
import org.eclipse.sumo.libtraci.TraCIPosition;
// Import Vehicle class to interact with SUMO vehicles
// Lấy thông tin xe từ SUMO qua TraCI
import org.eclipse.sumo.libtraci.Vehicle;

// Import Image class for car image
// Hình ảnh đại diện cho xe

// Import MapUtil for coordinate transforms (was MapGraphics)
// import novik.MapUtil;

public class CarLayer {
    // Map to store cars with their ID and image view
    // Bản đồ lưu trữ các xe với ID và hình đại diện
    private final Map<String, ImageView> carImageViews;
    // Pane to display all car nodes
    // Pane để hiển thị tất cả các node xe
    private final Pane carLayerPane;

    // Images for different vehicle types
    private final Map<String, Image> vehicleImages;

    // Constructor: receives the Pane where cars will be displayed
    // Hàm khởi tạo: nhận Pane để hiển thị xe
    CarLayer(Pane carLayerPane) {

        carImageViews = new HashMap<>();
        this.carLayerPane = carLayerPane;

        vehicleImages = new HashMap<>();

        // cars
        vehicleImages.put("carblack", loadVehicleImage("/carblack.png"));
        vehicleImages.put("carblue", loadVehicleImage("/carblue.png"));
        vehicleImages.put("carred", loadVehicleImage("/carred.png"));

        // bus
        vehicleImages.put("bus", loadVehicleImage("/bus.png"));

        // trucks
        vehicleImages.put("truck", loadVehicleImage("/truck.png"));
        vehicleImages.put("truckwhite", loadVehicleImage("/truckwhite.png"));
        vehicleImages.put("truckyellow", loadVehicleImage("/truckyellow.png"));

        // supercars
        vehicleImages.put("supercargrey", loadVehicleImage("/supercargrey.png"));
        vehicleImages.put("supercarwhite", loadVehicleImage("/supercarwhite.png"));
        // If carLayerPane is null, clear its children (defensive, but should not
        // happen)
        // Nếu carLayerPane là null, xóa hết các node con (phòng trường hợp lỗi, nhưng
        // thường không xảy
        // ra)
        if (carLayerPane == null)

        {
            this.carLayerPane.getChildren().clear();
        }
    }

    // Load a vehicle image from resources
    // Load hình xe từ thư mục resources
    private Image loadVehicleImage(String imagePath) {
        try {
            String absolutePath = getClass().getResource(imagePath).toExternalForm();
            Image img = new Image(absolutePath);
            System.out.println("Loaded image: " + imagePath);
            return img;
        } catch (NullPointerException e) {
            System.err.println("Failed to load image: " + imagePath);
            return null;
        }
    }

    // Get appropriate image for vehicle based on type (prioritize special types)
    private Image getImageForVehicle(String vehicleId) {
        String typeId = Vehicle.getTypeID(vehicleId);
        String classId = Vehicle.getVehicleClass(vehicleId);
        String type = typeId == null ? "" : typeId.toLowerCase();
        String vclass = classId == null ? "" : classId.toLowerCase();

        // Priority: special vehicle types first
        if (type.contains("supercar") || vclass.contains("supercar")) {
            if (type.contains("grey") && vehicleImages.get("supercargrey") != null)
                return vehicleImages.get("supercargrey");
            if (type.contains("white") && vehicleImages.get("supercarwhite") != null)
                return vehicleImages.get("supercarwhite");
        }
        if (type.contains("bus") || vclass.contains("bus")) {
            if (vehicleImages.get("bus") != null)
                return vehicleImages.get("bus");
        }
        if (type.contains("truck") || vclass.contains("truck") || type.contains("trailer") || vclass.contains("trailer")) {
            if (type.contains("white") && vehicleImages.get("truckwhite") != null)
                return vehicleImages.get("truckwhite");
            if (type.contains("yellow") && vehicleImages.get("truckyellow") != null)
                return vehicleImages.get("truckyellow");
            if (vehicleImages.get("truck") != null)
                return vehicleImages.get("truck");
        }
        // Priority: car with specific color
        if (type.contains("car") || vclass.contains("car")) {
            if (type.contains("blue") && vehicleImages.get("carblue") != null)
                return vehicleImages.get("carblue");
            if (type.contains("red") && vehicleImages.get("carred") != null)
                return vehicleImages.get("carred");
            if (vehicleImages.get("carblack") != null)
                return vehicleImages.get("carblack");
        }
        // Fallback: return carblack if no match
        if (vehicleImages.get("carblack") != null)
            return vehicleImages.get("carblack");
        // Final fallback: null
        return null;
    }

    // Get appropriate size for each vehicle type (English comments, new logic)
    private double[] getSizeForVehicle(String vehicleId) {
        String typeId = Vehicle.getTypeID(vehicleId);
        String classId = Vehicle.getVehicleClass(vehicleId);
        String type = typeId == null ? "" : typeId.toLowerCase();
        String vclass = classId == null ? "" : classId.toLowerCase();

        // Supercar: width 3.5, length 8.5
        if (type.contains("supercar") || vclass.contains("supercar")) {
            return new double[] {3.5, 8.5};
        }
        // Bus: width 3, length 10
        if (vclass.contains("bus") || type.contains("bus")) {
            return new double[] {3, 10};
        }
        // Truck: width 3, length 9
        if (type.contains("truck") || vclass.contains("truck") || type.contains("trailer") || vclass.contains("trailer")) {
            return new double[] {3, 9};
        }
        // All car types: width 3, length 8
        if (type.contains("car") || vclass.contains("car")) {
            return new double[] {3, 8};
        }
        // fallback: car
        return new double[] {3, 8};
    }

    // Create a new car appearance and add it to the pane
    // Tạo hình xe mới và thêm vào pane
    private void createCarImageView(String carID) {
        // Get the appropriate image for this vehicle type
        // Lấy hình ảnh phù hợp cho loại xe này
        Image vehicleImage = getImageForVehicle(carID);

        if (vehicleImage == null) {
            System.err.println("No image available for vehicle: " + carID);
            return;
        }

        ImageView carView = new ImageView(vehicleImage);

        // Get the appropriate size for this vehicle type
        // Lấy kích thước phù hợp cho loại xe này
        double[] size = getSizeForVehicle(carID);
        carView.setFitWidth(size[0]);
        carView.setFitHeight(size[1]);

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
                Point2D javaPos = MapUtil.worldToScreen(new Point2D(sumoPos.getX(), sumoPos.getY()));
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

        // 7. For each new car detected in SUMO, create its appearance and add to the
        // pane
        // 7. Với mỗi xe mới xuất hiện trong SUMO, tạo hình xe và thêm vào pane
        for (String carId : newCarIds) {
            createCarImageView(carId);
            ImageView carView = carImageViews.get(carId);

            if (carView != null) {
                // Get and convert position as above
                // Lấy và chuyển đổi vị trí như trên
                TraCIPosition sumoPosition = Vehicle.getPosition(carId, false);
                Point2D javaPosition = MapUtil.worldToScreen(new Point2D(sumoPosition.getX(), sumoPosition.getY()));
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
