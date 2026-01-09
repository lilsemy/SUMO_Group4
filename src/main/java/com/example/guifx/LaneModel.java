package com.example.guifx;

import org.eclipse.sumo.libtraci.Lane;
import org.eclipse.sumo.libtraci.TraCIPositionVector;

public class LaneModel {
    private String LaneId;
    private TraCIPositionVector LaneShape;


    public LaneModel(String laneId, TraCIPositionVector Shape){
        this.LaneId = LaneId;
        this.LaneShape = Shape;
    }

    public TraCIPositionVector getShape(){return LaneShape;}
    public String getId(){return LaneId;}
}
