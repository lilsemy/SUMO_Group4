package com.example.guifx;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Vehicle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.*;


public class CarLayer {
    // Map to store cars with their ID and image view
    private final Map<String, ImageView> carImageViews;
    // Pane to display all car nodes
    private final Pane carLayerPane;

    // Images for different vehicle types
    private final Image carImage;
    private final Image busImage;
    private final Image truckImage;

    // Constructor: receives the Pane where cars will be displayed
    CarLayer(Pane carLayerPane) {

        carImageViews = new HashMap<>();
        this.carLayerPane = carLayerPane;
        
        // Load all vehicle images
        this.carImage = loadVehicleImage("/carblack.png");
        this.busImage = loadVehicleImage("/bus.png");
        this.truckImage = loadVehicleImage("/truck.png");
        
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
    
    private Image getImageForVehicle(String vehicleId) {
        String typeId = Vehicle.getTypeID(vehicleId);
        String classId = Vehicle.getVehicleClass(vehicleId);
        String type = typeId == null ? "" : typeId.toLowerCase();
        String vclass = classId == null ? "" : classId.toLowerCase();

        if (vclass.contains("bus") || type.contains("bus")) {
            if (busImage != null) return busImage;
            return carImage;
        }
        if (vclass.contains("truck") || vclass.contains("trailer") || type.contains("truck") || type.contains("trailer")) {
            if (truckImage != null) return truckImage;
            return carImage;
        }
        return carImage;
    }

    private double[] getSizeForVehicle(String vehicleId) {
        String typeId = Vehicle.getTypeID(vehicleId);
        String classId = Vehicle.getVehicleClass(vehicleId);
        String type = typeId == null ? "" : typeId.toLowerCase();
        String vclass = classId == null ? "" : classId.toLowerCase();

        if (vclass.contains("bus") || type.contains("bus")) {
            return new double[]{9, 20};
        }
        if (vclass.contains("truck") || vclass.contains("trailer") || type.contains("truck") || type.contains("trailer")) {
            return new double[]{8, 17};
        }
        return new double[]{7, 15};
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
        
        // Get the appropriate size for this vehicle type
        double[] size = getSizeForVehicle(carID);
        carView.setFitWidth(size[0]);
        carView.setFitHeight(size[1]);
        
        // Keep aspect ratio
        carView.setPreserveRatio(true);
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
                // Get the car's position from SUMO (TraCI)
                TraCIPosition sumoPos = Vehicle.getPosition(carId, false);
                // Convert SUMO world coordinates to JavaFX screen coordinates
                Point2D javaPos = MapUtil.worldToScreen(
                        new Point2D(sumoPos.getX(), sumoPos.getY())
                );
                double w = carView.getFitWidth();
                double h = carView.getFitHeight();
                // Get the car's angle from SUMO and rotate the image accordingly
                double angle = Vehicle.getAngle(carId);
                carView.setRotate(angle);

                double radAngle = Math.toRadians(angle);
                // Calculate offset to position the car image correctly
                double offsetX = h / 2 * Math.sin(radAngle);
                double offsetY = -(h / 2) * Math.cos(radAngle);
                // Center the car image at the car's top position
                carView.setLayoutX(javaPos.getX() - w / 2.0 - offsetX);
                carView.setLayoutY(javaPos.getY() - h / 2.0 - offsetY);
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
                // Get and convert position as above
                TraCIPosition sumoPosition = Vehicle.getPosition(carId, false);
                Point2D javaPosition = MapUtil.worldToScreen(
                        new Point2D(sumoPosition.getX(), sumoPosition.getY())
                );
                double width = carView.getFitWidth();
                double height = carView.getFitHeight();
                carView.setLayoutX(javaPosition.getX() - width / 2.0);
                carView.setLayoutY(javaPosition.getY() - height / 2.0);

                // Set the car's rotation to match SUMO's angle
                double angle = Vehicle.getAngle(carId);
                // The car image should be aligned horizontally with the Ox axis
                carView.setRotate(angle);
            }
        }
    }
}