package com.example.guifx;

import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;

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
     * Returns the width and height for a vehicle type.
     *
     * @param typeId vehicle type id (e.g. car/truck/bus)
     * @return array [width, height]
     */
    private double[] getSizeForVehicleType(String typeId) {
        String type = typeId == null ? "" : typeId.toLowerCase();

        if (type.contains("bus")) {
            return new double[]{9, 20};
        }
        if (type.contains("truck") || type.contains("trailer")) {
            return new double[]{8, 17};
        }
        return new double[]{7, 15};
    }

    /**
     * Returns the appropriate image for a vehicle type + color.
     */
    private Image getImageForVehicleType(String typeId, VehicleColor color) {
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

    // NOTE: TraCI-based getSizeForVehicle(vehicleId) was removed in this merge version
    // because this class now renders from VehicleUiState snapshots (typeId is already present).

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
     *
     */
    private void createCarImageViewFromSnapshot(VehicleUiState vs) {
        Image vehicleImage = getImageForVehicleType(vs.type(), vs.color());
        if (vehicleImage == null) {
            System.err.println("No image available for vehicle: " + vs.id());
            return;
        }

        ImageView carView = new ImageView(vehicleImage);
        double[] size = getSizeForVehicleType(vs.type());
        carView.setFitWidth(size[0]);
        carView.setFitHeight(size[1]);
        carView.setPreserveRatio(true);
        carView.setSmooth(true);

        final String carId = vs.id();

        // Hover: show info while pointer is on the vehicle (unless another is pinned)
        carView.setOnMouseEntered(evt -> {
            hoveredVehicleId = carId;
            if (pinnedVehicleId == null && vehicleClickListener != null) {
                vehicleClickListener.onVehicleFocusChanged(carId, false);
            }
        });
        carView.setOnMouseExited(evt -> {
            if (Objects.equals(hoveredVehicleId, carId)) {
                hoveredVehicleId = null;
            }
            if (pinnedVehicleId == null && vehicleClickListener != null) {
                vehicleClickListener.onVehicleFocusChanged(null, false);
            }
        });

        // Click: pin selection
        carView.setOnMouseClicked(evt -> {
            evt.consume();
            pinnedVehicleId = carId;
            if (vehicleClickListener != null) {
                vehicleClickListener.onVehicleFocusChanged(carId, true);
            }
        });

        carImageViews.put(carId, carView);
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

        Set<String> newCarIds = new HashSet<>(activeCarIds);


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

        for (String carId : newCarIds) {
            VehicleUiState vs = stateMap.get(carId);
            if (vs == null) continue;

            createCarImageViewFromSnapshot(vs);
            ImageView carView = carImageViews.get(carId);

            if (carView != null) {
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
