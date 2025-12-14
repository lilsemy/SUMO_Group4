package com.example.guifx;

import java.util.*;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.Vehicle;

/**
 * VehicleController is one of the sub controller classes that controls the vehicles in the
 * simulation
 */
public class VehicleController {
  // for storing our vehicles in a map (dictionary);
  // key  = vehicle id
  // value = vehicle object
  private Map<String, VehicleModel> vehiclesList;
  private int vehicleCounter = 0;

  // Available vehicle types and routes for random injection
  private static final String[] VEHICLE_TYPES = {"car", "bus", "truck"};
  private static final String[] ROUTES = {"r1", "r2"};
  private final Random random = new Random();

  // removed Connection object, because it shouldn't see it
  public VehicleController() {
    this.vehiclesList = new HashMap<>();
  }

  /**
   * Creates and injects a vehicle with random type and random route
   *
   * @return the created VehicleModel if successful, null if injection failed
   */
  public VehicleModel createAndInjectRandomVehicle() {
    String randomType = VEHICLE_TYPES[random.nextInt(VEHICLE_TYPES.length)];
    String randomRoute = ROUTES[random.nextInt(ROUTES.length)];
    byte randomLane = (byte) random.nextInt(2); // lane 0 or 1

    return createAndInjectVehicle(randomType, randomRoute, randomLane);
  }

  /**
   * Gets the available vehicle types
   *
   * @return array of vehicle type IDs
   */
  public static String[] getVehicleTypes() {
    return VEHICLE_TYPES;
  }

  /**
   * Gets the available routes
   *
   * @return array of route IDs
   */
  public static String[] getRoutes() {
    return ROUTES;
  }

  /**
   * Creates and injects a vehicle
   *
   * @param typeId vehicle type
   * @param routeId route ID
   * @param laneId lane ID
   * @return the created VehicleModel if successful, null if injection failed
   */
  public VehicleModel createAndInjectVehicle(String typeId, String routeId, byte laneId) {
    vehicleCounter++;
    String id = "id" + vehicleCounter;

    VehicleModel vehicle = new VehicleModel(id, typeId, routeId, laneId);
    // inject it into SUMO
    boolean success = injectVehicle(vehicle);

    if (success) {
      return vehicle;
    } else {
      return null; // injection failed
    }
  }

  /**
   * @param vehicle
   * @return true if injection succeeded, false otherwise
   */
  public boolean injectVehicle(VehicleModel vehicle) {
    try {
      Vehicle.add(
          vehicle.getId(),
          vehicle.getRouteId(),
          vehicle.getTypeId(),
          String.valueOf(vehicle.getDepart()),
          String.valueOf(vehicle.getLaneId()),
          String.valueOf(vehicle.getPos()),
          String.valueOf(vehicle.getSpeed()),
          String.valueOf(vehicle.getLaneId()));

      vehiclesList.put(vehicle.getId(), vehicle);
      System.out.println(
          "Injected vehicle: "
              + vehicle.getId()
              + " ["
              + vehicle.getTypeId()
              + "] on route "
              + vehicle.getRouteId());
      return true;
    } catch (Exception e) {
      System.err.println("Failed to inject vehicle " + vehicle.getId() + ": " + e.getMessage());
      // Rollback counter since injection failed
      vehicleCounter--;
      return false;
    }
  }

  /**
   * @return Speed of Vehicle
   * @param vehicleId
   * @throws Exception
   */
  public double getVehicleSpeed(String vehicleId) throws Exception {
    if (vehiclesList.containsKey(vehicleId)) {
      return vehiclesList.get(vehicleId).getSpeed();
    } else {
      System.out.println("Error! Vehicle " + vehicleId + " not found in simulation.");
      return 0;
    }
  }

  public String getLastVehicleId() {
    if (vehiclesList.isEmpty()) return null;
    return "id" + vehicleCounter; // letzte erzeugte ID
  }

  /**
   * @return List of Vehicles
   */
  public Map<String, VehicleModel> getVehiclesMap() {
    return vehiclesList;
  }

  /**
   * @return vehicle
   * @param id
   */
  public VehicleModel getVehicle(String id) { // what
    return vehiclesList.get(id);
  }

  /**
   * @return sets camera of sumo-gui on given vehicle
   * @param viwId, vehId
   * @throws Exception
   */
  public void trackVehicle(String viwId, String vehId) throws Exception {

    if (getIds().contains(vehId)) {
      // problem
      // GUI.trackVehicle(viwId, vehId);
    } else {
      System.out.println("Warning! Car left the Map or was deleted.");
    }
  }

  /**
   * @return IDs of all Vehicle in the simulation
   * @throws Exception
   */
  public List<String> getIds() throws Exception {

    List<String> IDList = Vehicle.getIDList();
    return IDList;
  }

  public void updateFromSimulation() throws Exception {

    for (String id : Vehicle.getIDList()) {
      double speed = Vehicle.getSpeed(id);
      TraCIPosition pos = Vehicle.getPosition(id, false);
      double angle = Vehicle.getAngle(id);

      // Get our local object OR create a new one if needed
      VehicleModel v = vehiclesList.getOrDefault(id, new VehicleModel(id));

      v.setSpeed(speed);
      v.setPosition(pos.getX(), pos.getY());
      v.setAngle(angle);

      vehiclesList.put(id, v);
    }
  }
}
