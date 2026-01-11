package com.example.guifx;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Vehicle;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.*;
import javafx.scene.input.MouseEvent;

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

    private final SimulationController simController;

    /**
     * Constructs a CarLayer and loads vehicle images
     * 
     * @param carLayerPane Pane to display vehicles
     */
    CarLayer(Pane carLayerPane, SimulationController simController) {
        carImageViews = new HashMap<>();
        this.carLayerPane = carLayerPane;
        this.simController = simController;

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

        // Click on empty space => clear pinned selection
        if (this.carLayerPane != null) {
            this.carLayerPane.addEventFilter(MouseEvent.MOUSE_PRESSED, evt -> {
                if (evt.getTarget() == this.carLayerPane) {
                    clearSelection();
                }
            });
        }

        if (carLayerPane == null) {
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
     * Returns the appropriate image for a vehicle
     * 
     * @param vehicleId vehicle ID
     * @return Image for the vehicle
     */
    private Image getImageForVehicle(String vehicleId) {
        VehicleModel v = simController.getVehicleController().getVehicle(vehicleId);
        String typeId = v.getTypeId();
        VehicleColor color = v.getColor();
        String type = typeId == null ? "" : typeId.toLowerCase();

        return switch (type) {
            case "car" -> switch (color) {
                case VehicleColor.RED -> carRedImage;
                case VehicleColor.WHITE -> carWhiteImage;
                case VehicleColor.YELLOW -> carYellowImage;
                default -> carBlackImage;
            };
            case "truck" -> switch (color) {
                case VehicleColor.RED -> truckRedImage;
                case VehicleColor.WHITE -> truckWhiteImage;
                case VehicleColor.YELLOW -> truckYellowImage;
                default -> truckBlackImage;
            };
            case "bus" -> switch (color) {
                case VehicleColor.RED -> busRedImage;
                case VehicleColor.WHITE -> busWhiteImage;
                case VehicleColor.YELLOW -> busYellowImage;
                default -> busBlackImage;
            };
            default -> carBlackImage;
        };

    }

    /**
     * Returns the width and height for a vehicle
     * 
     * @param vehicleId vehicle ID
     * @return array [width, height]
     */
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

    /**
     * Callback used by UI layer (GUI) to react on hover / click selection.
     */
    public interface VehicleClickListener {
        /**
         * @param vehicleId vehicle id or null when nothing is selected/hovered
         * @param isPinned true if this is a click selection (pinned), false if it's just hover
         */
        void onVehicleFocusChanged(String vehicleId, boolean isPinned);
    }

    private VehicleClickListener vehicleClickListener;
    private String pinnedVehicleId;
    private String hoveredVehicleId;

    public void setVehicleClickListener(VehicleClickListener listener) {
        this.vehicleClickListener = listener;
    }

    /** Clear pinned selection (e.g. when clicking on empty space). */
    public void clearSelection() {
        pinnedVehicleId = null;
        hoveredVehicleId = null;
        if (vehicleClickListener != null) {
            vehicleClickListener.onVehicleFocusChanged(null, true);
        }
    }

    /**
     * Creates a new car ImageView and adds it to the pane
     * 
     * @param carID vehicle ID
     */
    private void createCarImageView(String carID) {
        Image vehicleImage = getImageForVehicle(carID);
        if (vehicleImage == null) {
            System.err.println("No image available for vehicle: " + carID);
            return;
        }

        ImageView carView = new ImageView(vehicleImage);
        double[] size = getSizeForVehicle(carID);
        carView.setFitWidth(size[0]);
        carView.setFitHeight(size[1]);
        carView.setPreserveRatio(true);
        carView.setSmooth(true);

        // Hover: show info while pointer is on the vehicle (unless another is pinned)
        carView.setOnMouseEntered(evt -> {
            hoveredVehicleId = carID;
            if (pinnedVehicleId == null && vehicleClickListener != null) {
                vehicleClickListener.onVehicleFocusChanged(carID, false);
            }
        });
        carView.setOnMouseExited(evt -> {
            if (Objects.equals(hoveredVehicleId, carID)) {
                hoveredVehicleId = null;
            }
            if (pinnedVehicleId == null && vehicleClickListener != null) {
                vehicleClickListener.onVehicleFocusChanged(null, false);
            }
        });

        // Click: pin selection
        carView.setOnMouseClicked(evt -> {
            evt.consume();
            pinnedVehicleId = carID;
            if (vehicleClickListener != null) {
                vehicleClickListener.onVehicleFocusChanged(carID, true);
            }
        });

        carImageViews.put(carID, carView);
        carLayerPane.getChildren().add(carView);
    }

    /**
     * Updates all cars: adds new cars, updates positions and rotations, removes disappeared cars
     */
    public void updateCars(Collection<String> list) {
        //List<String> ids = Vehicle.getIDList();
        Set<String> knownVehicleIds = new HashSet<>(list);
        Set<String> newVehicleIds = new HashSet<>(knownVehicleIds);

        Iterator<Map.Entry<String, ImageView>> it = carImageViews.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, ImageView> entry = it.next();
            String carId = entry.getKey();
            ImageView carView = entry.getValue();

            if (knownVehicleIds.contains(carId)) {

                //safety check. If car is queued up and not yet active, don't call traas methods
                if(!Vehicle.getIDList().contains(carId)){
                    continue;
                }

                newVehicleIds.remove(carId);


                TraCIPosition sumoPos = Vehicle.getPosition(carId, false);
                Point2D javaPos = MapUtil.worldToScreen(new Point2D(sumoPos.getX(), sumoPos.getY()));
                double w = carView.getFitWidth();
                double h = carView.getFitHeight();
                double angle = Vehicle.getAngle(carId);
                carView.setRotate(angle);

                double radAngle = Math.toRadians(angle);
                double offsetX = h / 2 * Math.sin(radAngle);
                double offsetY = -(h / 2) * Math.cos(radAngle);
                carView.setLayoutX(javaPos.getX() - w / 2.0 - offsetX);
                carView.setLayoutY(javaPos.getY() - h / 2.0 - offsetY);
            } else {
                // if the removed car was pinned/hovered, clear and update UI
                boolean removedPinned = Objects.equals(pinnedVehicleId, carId);
                boolean removedHover = Objects.equals(hoveredVehicleId, carId);

                carLayerPane.getChildren().remove(carView);
                it.remove();

                if (removedPinned) {
                    pinnedVehicleId = null;
                }
                if (removedHover) {
                    hoveredVehicleId = null;
                }

                if ((removedPinned || removedHover) && pinnedVehicleId == null && vehicleClickListener != null) {
                    vehicleClickListener.onVehicleFocusChanged(null, true);
                }
            }
        }

        for (String carId : newVehicleIds) {
            //Don't create image for not yet active vehicles
            if(!Vehicle.getIDList().contains(carId)){
                continue;
            }

            createCarImageView(carId);
            ImageView carView = carImageViews.get(carId);
            if (carView != null) {
                TraCIPosition sumoPosition = Vehicle.getPosition(carId, false);
                Point2D javaPosition = MapUtil.worldToScreen(new Point2D(sumoPosition.getX(), sumoPosition.getY()));
                double width = carView.getFitWidth();
                double height = carView.getFitHeight();
                carView.setLayoutX(javaPosition.getX() - width / 2.0);
                carView.setLayoutY(javaPosition.getY() - height / 2.0);
                double angle = Vehicle.getAngle(carId);
                carView.setRotate(angle);
            }
        }
    }
}
