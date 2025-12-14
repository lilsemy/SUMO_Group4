package com.example.guifx;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.LineChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** GUI is the class that controls the JavaFX GUI */
public class GUI {
  private GUIController controller;

  public GUI() throws Exception {}

  /*
   * EXPLANATION (New Fields):
   * These @FXML fields link to the new components added in GUI-view.fxml.
   * We replaced the TextField with a StackPane/Pane structure to draw the map.
   * - mapContainer: Holds the entire map view.
   * - laneLayer, trafficLightLayer, carLayer: Transparent Panes stacked on top of
   * each other.
   * We draw different parts of the simulation on different layers to manage them
   * easily.
   */

  @FXML private StackPane mapContainer;
  @FXML private Canvas backgroundCanvas;
  @FXML private Pane laneLayer;
  @FXML private Pane trafficLightLayer;
  @FXML private Pane carLayer;

  // Info Panel Components
  @FXML private VBox hoverInfoBox;
  @FXML private Label hoverInfoLabel;
  @FXML private Label vehicleCountLabel;
  @FXML private Label avgSpeedLabel;
  @FXML private Label simTimeLabel;
  @FXML private Label zoomLabel;
  @FXML private Label statusLabel;
  @FXML private LineChart<Number, Number> speedChart;

  // Alarm Button
  @FXML private Button alarmButton;
  @FXML private ImageView alarmIcon;

  @FXML private BorderPane rootPane;

  @FXML private TextField alarmVehicleCountField;

  // Getters for GUIController
  public StackPane getMapContainer() {
    return mapContainer;
  }

  public Canvas getBackgroundCanvas() {
    return backgroundCanvas;
  }

  public Pane getLaneLayer() {
    return laneLayer;
  }

  public Pane getTrafficLightLayer() {
    return trafficLightLayer;
  }

  public Pane getCarLayer() {
    return carLayer;
  }

  public VBox getHoverInfoBox() {
    return hoverInfoBox;
  }

  public Label getHoverInfoLabel() {
    return hoverInfoLabel;
  }

  public Label getVehicleCountLabel() {
    return vehicleCountLabel;
  }

  public Label getAvgSpeedLabel() {
    return avgSpeedLabel;
  }

  public Label getSimTimeLabel() {
    return simTimeLabel;
  }

  public Label getZoomLabel() {
    return zoomLabel;
  }

  public Label getStatusLabel() {
    return statusLabel;
  }

  public LineChart<Number, Number> getSpeedChart() {
    return speedChart;
  }

  public Button getAlarmButton() {
    return alarmButton;
  }

  public ImageView getAlarmIcon() {
    return alarmIcon;
  }

  public BorderPane getRootPane() {
    return rootPane;
  }

  public TextField getAlarmVehicleCountField() {
    return alarmVehicleCountField;
  }

  /**
   * Sets the SimulationController in order to insert Cars and change the view of the Sumo-GUI
   *
   * @param simulationController
   */
  public void setSimulationController(SimulationController simulationController) {
    controller = new GUIController(this, simulationController);
    controller.init();
  }

  @FXML
  public void commandTurnOnAlarms(ActionEvent e) {
    controller.toggleAlarm();
  }

  /** Inserts a car and sets the view on it, when Button is clicked */
  @FXML
  public void commandSpawnVehicle(ActionEvent e) {
    controller.spawnVehicle();
  }

  @FXML
  public void commandGetVehicleSpeed(ActionEvent e) {
    controller.getLastVehicleSpeed();
  }

  @FXML
  public void commandChangePhase(ActionEvent e) {
    controller.changePhase();
  }
}
