package com.example.guifx;

import org.eclipse.sumo.libtraci.TraCIPositionVector;

public class LaneModel {
    private String LaneId;
    private TraCIPositionVector LaneShape;
    String Edge;


    public LaneModel(String laneId, TraCIPositionVector Shape, String Edge){
        this.LaneId = LaneId;
        this.LaneShape = Shape;
        this.Edge = Edge;
    }

    public TraCIPositionVector getShape(){return LaneShape;}
    public String getLaneId(){return LaneId;}
    public String getEdge(){return Edge;}
}
