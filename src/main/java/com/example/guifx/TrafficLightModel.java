package com.example.guifx;

/**
 * TrafficLightModel is the model class of Traffic Lights, which stores all necessary attributes of a Traffic Light. The instances are initialized over the TrafficLightController
 */

public class TrafficLightModel {
    private String id;
    private double duration;
    private int phase;
    private String RedYellowGreenState;
    private String GroupID;

    /**
     * No argument Constructor for TrafficLightModel
     */
    public TrafficLightModel() {
    }

    /**
     * Constructor
     * @param id
     * @param phase
     * @param RedYellowGreenState
     */
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

    /**
     *
     * @return Traffic Light ID
     */
    public String getId() {
        return id;
    }

    /**
     * Setter method for Traffic Light Phase
     * @param phase
     */
    public void setPhase(int phase){
        this.phase = phase;
    }

    /**
     * Returns the current Traffic Light Phase in "RRYYGG" pattern
     * @return RedYellowGreenState
     */
    public String getRedYellowGreenState(){return RedYellowGreenState;}

    /**
     * Sets duration of current Phase
     * @param duration
     */
    public void setDuration(double duration){
        this.duration=duration;
    }

    /**
     * Traffic Lights are grouped into two clusters. This function returns the group ID
     * @return GroupID of Traffic Light
     */
    public String getGroupID(){
        return GroupID;
    }

    /**
     * Setter for the RedYellowGreenState Phase
     * @param redYellowGreenState
     */
    public void setRedYellowGreenState(String redYellowGreenState) {
        RedYellowGreenState = redYellowGreenState;
    }
}
