package com.example.guifx;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

//import org.eclipse.sumo.libtraci.Vehicle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.*;

/**
 * CarLayer is responsible for displaying vehicles in the simulation on a JavaFX Pane
 */
public class CarLayer {
    // Map to store cars with their ID and ImageView
    private final Map<String, ImageView> carImageViews;
    // Pane to display all car nodes
    private final Pane carLayerPane;
    // Images for different vehicle types
    private final Image carBlackImage;
    private final Image carRedImage;
    private final Image carWhiteImage;
    private final Image carYellowImage;

    private final Image busBlackImage;
    private final Image busYellowImage;
    private final Image busRedImage;
    private final Image busWhiteImage;

    private final Image truckBlackImage;
    private final Image truckRedImage;
    private final Image truckWhiteImage;
    private final Image truckYellowImage;

    //private final SimulationController simController;

    /**
     * Constructs a CarLayer and loads vehicle images
     * 
     * @param carLayerPane Pane to display vehicles
     */
    CarLayer(Pane carLayerPane) {
        carImageViews = new HashMap<>();
        this.carLayerPane = carLayerPane;
        //this.simController = simController;

        this.carBlackImage = loadVehicleImage("/carBlack.png");
        this.carRedImage = loadVehicleImage("/carRed.png");
        this.carWhiteImage = loadVehicleImage("/carWhite.png");
        this.carYellowImage = loadVehicleImage("/carYellow.png");

        this.busBlackImage = loadVehicleImage("/busBlack.png");
        this.busRedImage = loadVehicleImage("/busRed.png");
        this.busWhiteImage = loadVehicleImage("/busWhite.png");
        this.busYellowImage = loadVehicleImage("/busYellow.png");

        this.truckBlackImage = loadVehicleImage("/truckBlack.png");
        this.truckRedImage = loadVehicleImage("/truckRed.png");
        this.truckWhiteImage = loadVehicleImage("/truckWhite.png");
        this.truckYellowImage = loadVehicleImage("/truckYellow.png");


        // FIX: Only clear if pane is NOT null
        //if (carLayerPane == null) {
        if (carLayerPane != null) {
            this.carLayerPane.getChildren().clear();
        }
    }

    /**
     * Loads a vehicle image from resources
     * 
     * @param imagePath path to image
     * @return Image object or null if loading failed
     */
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



    /**
     * Returns the width and height for a vehicle
     * 
     *
     * @return array [width, height]
     */
    private double[] getSizeForVehicle(String typeId) {
        //String typeId = Vehicle.getTypeID(vehicleId);
        //String classId = Vehicle.getVehicleClass(vehicleId);
        String type = typeId == null ? "" : typeId.toLowerCase();
        //String vclass = classId == null ? "" : classId.toLowerCase();

        if (type.contains("bus")) {
            return new double[]{9, 20};
        }
        if (type.contains("truck") || type.contains("trailer") ) {
            return new double[]{8, 17};
        }
        return new double[]{7, 15};
    }

    /**
     * Returns the appropriate image for a vehicle
     *
     *
     * @return Image for the vehicle
     */
    private Image getImageForVehicleType(String typeId, VehicleColor color) {
        //VehicleModel v = simController.getVehicleController().getVehicle(vehicleId);
        //String typeId = v.getTypeId();
        //VehicleColor color = v.getColor();
        String type = typeId == null ? "" : typeId.toLowerCase();

        return switch (type) {
            case "car" -> switch (color) {
                case RED -> carRedImage;
                case WHITE -> carWhiteImage;
                case YELLOW -> carYellowImage;
                default -> carBlackImage;
            };
            case "truck" -> switch (color) {
                case RED -> truckRedImage;
                case WHITE -> truckWhiteImage;
                case YELLOW -> truckYellowImage;
                default -> truckBlackImage;
            };
            case "bus" -> switch (color) {
                case RED -> busRedImage;
                case WHITE -> busWhiteImage;
                case YELLOW -> busYellowImage;
                default -> busBlackImage;
            };
            default -> carBlackImage;
        };

    }

    /**
     * Creates a new car ImageView and adds it to the pane
     *
     *
     */
    private void createCarImageViewFromSnapshot(VehicleUiState vs) {
        Image vehicleImage = getImageForVehicleType(vs.type(),vs.color() );
        if (vehicleImage == null) {

            System.err.println("No image available for vehicle: " + vs.id());
            return;
        }

        ImageView carView = new ImageView(vehicleImage);
        double[] size = getSizeForVehicle(vs.type());
        carView.setFitWidth(size[0]);
        carView.setFitHeight(size[1]);
        carView.setPreserveRatio(true);
        carView.setSmooth(true);

        carImageViews.put(vs.id(), carView);
        carLayerPane.getChildren().add(carView);
    }

    /**
     * Updates all cars: adds new cars, updates positions and rotations, removes disappeared cars
     */
    public void updateCarsFromSnapshot(List<VehicleUiState> vehicleStates) {
        //List<String> ids = Vehicle.getIDList();
        Set<String> activeCarIds = new HashSet<>();
        Map<String, VehicleUiState> stateMap = new HashMap<>();
        for (VehicleUiState vs : vehicleStates) {
            activeCarIds.add(vs.id());
            stateMap.put(vs.id(),vs);
        }

        Set<String>newCarIds = new HashSet<>(activeCarIds);


        Iterator<Map.Entry<String, ImageView>> it = carImageViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ImageView> entry = it.next();
            String carId = entry.getKey();
            ImageView carView = entry.getValue();

            if (activeCarIds.contains(carId)) {
                newCarIds.remove(carId);
                //safety check. If car is queued up and not yet active, don't call traas methods
//                if(!Vehicle.getIDList().contains(carId)){
//                    continue;
//                }

                //newVehicleIds.remove(carId);


                //TraCIPosition sumoPos = Vehicle.getPosition(carId, false);
                VehicleUiState vs = stateMap.get(carId);
                Point2D javaPos = MapUtil.worldToScreen(new Point2D(vs.x(), vs.y()));
                double w = carView.getFitWidth();
                double h = carView.getFitHeight();
                double angle = vs.angle();

                carView.setRotate(angle);

                //
                double radAngle = Math.toRadians(angle);
                double offsetX = h / 2 * Math.sin(radAngle);
                double offsetY = -(h / 2) * Math.cos(radAngle);

                carView.setLayoutX(javaPos.getX() - w / 2.0 - offsetX);
                carView.setLayoutY(javaPos.getY() - h / 2.0 - offsetY);
            } else {
                carLayerPane.getChildren().remove(carView);
                it.remove();
            }
        }

        for (String carId : newCarIds) {
            //Don't create image for not yet active vehicles
            VehicleUiState vs = stateMap.get(carId);
            createCarImageViewFromSnapshot(vs);
           ImageView carView = carImageViews.get(carId);

//            if(!Vehicle.getIDList().contains(carId)){
//                continue;
//            }

//            createCarImageView(carId);

            if (carView != null) {
                //TraCIPosition sumoPosition = Vehicle.getPosition(carId, false);
                Point2D javaPosition = MapUtil.worldToScreen(new Point2D(vs.x(), vs.y()));
                double width = carView.getFitWidth();
                double height = carView.getFitHeight();
                carView.setLayoutX(javaPosition.getX() - width / 2.0);
                carView.setLayoutY(javaPosition.getY() - height / 2.0);
                double angle = vs.angle();
                carView.setRotate(angle);
            }
        }
    }





}
