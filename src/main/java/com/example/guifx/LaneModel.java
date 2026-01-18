package com.example.guifx;

import org.eclipse.sumo.libtraci.TraCIPositionVector;

/**
 * LaneModel is the model class of Lanes, which stores all necessary attributes of a Lane. The instances are initialized over the LaneController
 */
public class LaneModel {
    private String LaneId;
    private TraCIPositionVector LaneShape;
    String Edge;

    /**
     * Constructor
     * @param laneId
     * @param Shape
     * @param Edge
     */
    public LaneModel(String laneId, TraCIPositionVector Shape, String Edge){
        this.LaneId = laneId;
        this.LaneShape = Shape;
        this.Edge = Edge;
    }

    /**
     * Returns the shape of a lane in form of a TraCIPositionVector
     * @return TraCIPositionVector
     */
    public TraCIPositionVector getShape(){return LaneShape;}

    /**
     *
     * @return LaneID
     */
    public String getLaneId(){return LaneId;}

    /**
     * Return the EdgeID, on which the Lane is on
     * @return EdgeID
     */
    public String getEdge(){return Edge;}
}
