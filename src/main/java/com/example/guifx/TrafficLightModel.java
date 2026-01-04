package com.example.guifx;

/**
 * TrafficLightState is a model class for Traffic Lights
 */

public class TrafficLightModel {
    private String id;
    private double duration;
    private int phase;
    private String RedYellowGreenState;
    private String GroupID;


    public TrafficLightModel() {
    }

    public TrafficLightModel(String id, int phase, String RedYellowGreenState)
    {
        this.id = id;
        this.phase = phase;
        if (this.phase == 0) {
            this.duration = 80;
        } else if (this.phase == 2) {
            this.duration = 80;
        }
        else {
            this.duration = 10;
        }
        this.RedYellowGreenState = RedYellowGreenState;
        if (id.equals("tl1") || id.equals("tl2") || id.equals("tl3")) {
            this.GroupID = "TL1";
        }
        else {
            this.GroupID = "TL2";
        }
    }

    public String getId() {
        return id;
    }

    public int getPhase() {
        return phase;
    }

    public double getDuration() {
        return duration;
    }

    public void setPhase(int phase){
        this.phase = phase;
    }

    public String getRedYellowGreenState(){return RedYellowGreenState;}

    public void setDuration(double duration){
        this.duration=duration;
    }

    public String getGroupID(){
        return GroupID;
    }


}
