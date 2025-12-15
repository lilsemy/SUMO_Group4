package com.example.guifx;

// Import 2D point class from JavaFX for x, y coordinates
import java.util.*;
import javafx.geometry.Point2D;
// Import Pane class to contain car nodes in JavaFX
import javafx.scene.image.Image;
// Import ImageView class to display images in JavaFX
import javafx.scene.image.ImageView;
// Import utility classes for collections and lists
import javafx.scene.layout.Pane;
// Import TraCIPosition to get vehicle position from SUMO via TraCI
import org.eclipse.sumo.libtraci.TraCIPosition;
// Import Vehicle class to interact with SUMO vehicles
import org.eclipse.sumo.libtraci.Vehicle;

public class CarLayer {
    // Map to store cars with their ID and image view
    private final Map<String, ImageView> carImageViews;
    // Pane to display all car nodes
    private final Pane carLayerPane;

    // Images for different vehicle types
    private final Map<String, Image> vehicleImages;

    // Constructor: receives the Pane where cars will be displayed
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
        // If carLayerPane is null, clear its children (defensive, but should not happen)
        if (carLayerPane == null) {
            this.carLayerPane.getChildren().clear();
        }
    }

    // Load a vehicle image from resources
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

    // Get appropriate size for each vehicle type (in SUMO meters, will be scaled)
    private double[] getSizeForVehicle(String vehicleId) {
        String typeId = Vehicle.getTypeID(vehicleId);
        String classId = Vehicle.getVehicleClass(vehicleId);
        String type = typeId == null ? "" : typeId.toLowerCase();
        String vclass = classId == null ? "" : classId.toLowerCase();

        // Supercar: width 2.5, length 8
        if (type.contains("supercar") || vclass.contains("supercar")) {
            return new double[] {2.5, 5.8};
        }
        // Bus: width 2.5m, length 12m
        if (vclass.contains("bus") || type.contains("bus")) {
            return new double[] {3, 15.3};
        }
        // Truck: width 2.5m, length 8m
        if (type.contains("truck") || vclass.contains("truck") || type.contains("trailer") || vclass.contains("trailer")) {
            return new double[] {2.5, 7.8};
        }
        // All car types: width 2.5m, length 5.5m
        if (type.contains("car") || vclass.contains("car")) {
            return new double[] {2.5, 5.5};
        }
        // fallback: car
        return new double[] {2.5, 5.5};
    }

    // Create a new car appearance and add it to the pane
    private void createCarImageView(String carID) {
        // Get the appropriate image for this vehicle type
        Image vehicleImage = getImageForVehicle(carID);

        if (vehicleImage == null) {
            System.err.println("No image available for vehicle: " + carID);
            return;
        }

        ImageView carView = new ImageView(vehicleImage);

        // Get the appropriate size for this vehicle type (in meters) and scale to pixels
        double[] size = getSizeForVehicle(carID);
        double scaledWidth = MapUtil.metersToPixels(size[0]);
        double scaledHeight = MapUtil.metersToPixels(size[1]);
        carView.setFitWidth(scaledWidth);
        carView.setFitHeight(scaledHeight);

        // Set to False to allow independent width/height scaling
        carView.setPreserveRatio(false);
        // Smooth image quality
        carView.setSmooth(true);

        carImageViews.put(carID, carView);
        carLayerPane.getChildren().add(carView);
    }

    // Update all cars: add new, update position, remove disappeared
    public void updateCars() {
        // 1. Get the list of all vehicle IDs currently present in the SUMO simulation
        List<String> ids = Vehicle.getIDList();
        // 2. Convert the list to a set for fast lookup (active cars)
        Set<String> activeCarIds = new HashSet<>(ids);
        // 3. Create a set of new car IDs (will remove known cars later)
        Set<String> newCarIds = new HashSet<>(activeCarIds);

        // 4. Iterate through all cars currently managed (displayed)
        Iterator<Map.Entry<String, ImageView>> it = carImageViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ImageView> entry = it.next();
            String carId = entry.getKey();
            ImageView carView = entry.getValue();

            // 5. If the car is still active in SUMO
            if (activeCarIds.contains(carId)) {
                // Remove from newCarIds (not a new car)
                newCarIds.remove(carId);
                // Update car position and rotation
                updateCarPosition(carId, carView);
            } else {
                // 6. If the car no longer exists in SUMO, remove it from the pane and map
                carLayerPane.getChildren().remove(carView);
                it.remove();
            }
        }

        // 7. For each new car detected in SUMO, create its appearance and add to the pane
        for (String carId : newCarIds) {
            createCarImageView(carId);
            ImageView carView = carImageViews.get(carId);
            if (carView != null) {
                updateCarPosition(carId, carView);
            }
        }
    }

    // Update a single car's position and rotation (always use this for all cars)
    private void updateCarPosition(String carId, ImageView carView) {
        TraCIPosition sumoPos = Vehicle.getPosition(carId, false);
        Point2D javaPos = MapUtil.worldToScreen(new Point2D(sumoPos.getX(), sumoPos.getY()));
        double w = carView.getFitWidth();
        double h = carView.getFitHeight();
        double angle = Vehicle.getAngle(carId);
        carView.setRotate(angle);
        double radAngle = Math.toRadians(angle);
        double offsetX = (h / 2.0) * Math.sin(radAngle);
        double offsetY = -(h / 2.0) * Math.cos(radAngle);
        carView.setLayoutX(javaPos.getX() - w / 2.0 - offsetX);
        carView.setLayoutY(javaPos.getY() - h / 2.0 - offsetY);
    }
}