package com.example.guifx;

/** SimulationController orchestrates the whole simulation */
public class SimulationController {
  private final SumoConnection connection;
  private final VehicleController vehicleController;
  private final TrafficLightController tlController;

  // TODO
  // private final EdgeController edgeController;

  private volatile boolean running =
      true; // starts true (volatile booleans have multi-thread visibility)

  // Stress test counters
  private int stressVehiclesRemaining = 0;
  private int stepCounter = 0;
  private int injectionStepInterval = 10; // every 10 steps -> 10*0.05s/step = every 0.5s
  private boolean stressTestActive = false;

  /**
   * @throws Exception
   */
  public SimulationController() throws Exception {
    this.connection = new SumoConnection();
    this.connection.connect();

    vehicleController = new VehicleController();
    tlController = new TrafficLightController();
    // makeConnection(); happens in main
  }

  /** Establishes connection */
  public void makeConnection() {
    /*
     * EXPLANATION (Why this is commented out):
     * Originally, this method started a separate background Thread to run the
     * simulation loop continuously.
     * However, when rendering the simulation in JavaFX (the GUI), we must update
     * the screen on the "JavaFX Application Thread".
     * If we have a background thread like this running 'while(running)', it often
     * runs too fast or desynchronized from the screen refresh rate (60fps).
     * Worse, updating UI elements from this background thread would cause
     * "Not on FX Application Thread" errors.
     *
     * SOLUTION:
     * We disable this loop. Instead, the 'GUI' class controls the loop using an
     * 'AnimationTimer'.
     * The GUI calls 'singleStep()' once per frame. This ensures the simulation
     * advances exactly one step before we try to draw it.
     */

    /*
     * new Thread(() -> {
     * try {
     *
     * while(running){
     * connection.doStep();
     * //vehicleController.updateCars or similar method needs to be implemented
     * (also for every other controller)
     * }
     * } catch (Exception e) {
     * //System.err.println("Error during simulation step: " + stepEx.getMessage());
     * //stepEx.printStackTrace();
     * // Optionally: pause simulation or notify GUI
     * running = false;
     * } finally {
     * //the finally block ALWAYS executes: if try finishes; catch triggers or not;
     * exception is thrown
     * connection.close();
     * System.out.println("Simulation stopped cleanly.");
     * }
     * }).start();
     */
  }

  /**
   * EXPLANATION: This method advances the simulation by exactly one step (0.05s). It is called by
   * the GUI's AnimationTimer loop. Useing this instead of the while-loop in makeConnection so the
   * GUI stays responsive and synchronized.
   */
  public void singleStep() throws Exception {
    if (running && connection.isConnected()) {
      connection.doStep();
      stepCounter++;

      // stress testing only triggers when stressTestActive == true
      if (stressTestActive
          && stressVehiclesRemaining > 0
          && stepCounter % injectionStepInterval == 0) {

        // Use random vehicle type and random route for stress test
        VehicleModel injected = vehicleController.createAndInjectRandomVehicle();

        // Only decrement count if injection was successful
        if (injected != null) {
          stressVehiclesRemaining--;
          System.out.println("Stress test: " + stressVehiclesRemaining + " vehicles remaining");

          if (stressVehiclesRemaining == 0) {
            stressTestActive = false;
            System.out.println("Stress test completed!");
          }
        }
        // If injection failed, we'll try again next interval (don't decrement)
      }

      vehicleController.updateFromSimulation();
    }
  }

  /**
   * Method that lets the user perform a stress test
   *
   * @param count the amount of cars for the stress test
   */
  public void startStressTest(int count) {
    stressVehiclesRemaining = count;
    stressTestActive = true;
    stepCounter = 0;
  }

  /**
   * Method to check if stress test is active
   *
   * @return true if stress test is active, false otherwise
   */
  public boolean isStressTestActive() {
    return stressTestActive;
  }

  /**
   * Method to get the amount of vehicles remaining to be spawned in stress test
   *
   * @return amount of vehicles remaining
   */
  public int getStressVehiclesRemaining() {
    return stressVehiclesRemaining;
  }

  /** Method to stop the stress test */
  public void stopStressTest() {
    stressTestActive = false;
    stressVehiclesRemaining = 0;
  }

  /** Stops the simulation loop cleanly. */
  public void stopSimulation() {
    running = false; // shutdown simulation
  }

  /** Restarts the simulation loop after stopping. */
  public void startSimulation() {
    if (running) return; // already running

    running = true;
    makeConnection(); // start new thread
  }

    // Originally in GUI, but created a new abstraction, since the GUI should only
    // see the main controller
    public void spawnVehicle() {
        // Delegate vehicle creation and injection entirely to VehicleController
        // Uses random vehicle type instead of fixed "car"
        VehicleModel car = vehicleController.createAndInjectRandomVehicle();
        if (car != null) {
            // Optionally track the vehicle in SUMO GUI
            try {
                vehicleController.trackVehicle("View #0", car.getId());
            } catch (Exception e) {
                // Ignore tracking errors
            }
            System.out.println("Added a vehicle: " + car.getId() + " [" + car.getTypeId() + "]");
        } else {
            System.out.println("Failed to spawn vehicle - spawn point may be blocked");
        }
    }

    public void changePhase() {
        tlController.changePhase();
    }

    public VehicleController getVehicleController() {
        return vehicleController;
    }

    public TrafficLightController getTlController() {
        return tlController;
    }

    public SumoConnection getConnection() {
        return connection;
    }

}