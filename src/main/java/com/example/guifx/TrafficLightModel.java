package com.example.guifx;

/** TrafficLightState is a model class for Traffic Lights */
public class TrafficLightModel {
  private String id;
  private int duration;
  private int phase;
  private String RedYellowGreenState;

  public TrafficLightModel() {}

  public TrafficLightModel(String id, int phase, String RedYellowGreenState) {
    this.id = id;
    this.phase = phase;
    if (this.phase == 0) {
      this.duration = 8000000;
    } else if (this.phase == 2) {
      this.duration = 5000000;
    } else {
      this.duration = 10;
    }
    this.RedYellowGreenState = RedYellowGreenState;
  }

  public String getId() {
    return id;
  }

  public int getPhase() {
    return phase;
  }

  public int getDuration() {
    return duration;
  }

  public void setPhase(int phase) {
    this.phase = phase;
  }

  public String getRedYellowGreenState() {
    return RedYellowGreenState;
  }
}
