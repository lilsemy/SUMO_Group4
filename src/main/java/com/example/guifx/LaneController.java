package com.example.guifx;

import org.eclipse.sumo.libtraci.Lane;
import java.util.*;

/**
 * LaneController is a Controller to control, track and update all Lanes in the simulation
 */
public class LaneController {
    private List<String> LaneIds;
    private Map<String, LaneModel> lanes;
    private String[] notToPrint;
    private List<String> PrintLane;
    private List<String> EndLanes;

    /**
     * No argument Constructor of LaneController. Initializes all Lanes of the Simulation in form of an instance of the Model class LaneModel
     */
    public LaneController() {
        this.LaneIds = Lane.getIDList();
        this.lanes = new HashMap<>();
        this.notToPrint = new String[]{"L2", "L22", "L25", "L57", "L54", "L29", "L28", "L55", "L56", "L19", "L20", "L35", "L43", "L10", "L12", "L13", "L36", "L37", "L41", "L52", "L42", "L38", "L40", "L30", "L31"};
        this.PrintLane = new ArrayList<>();
        this.EndLanes = new ArrayList<>(List.of("-e21", "e51", "e32", "e31", "4804789#2", "e53", "e47", "e45", "e42", "e2"));
        for (String id : LaneIds){
            LaneModel lane = new LaneModel(id, Lane.getShape(id), Lane.getEdgeID(id));
            lanes.put(id, lane);
            if ((!Arrays.asList(notToPrint).contains(id)) && (!id.contains("-")) && (id.contains("L"))){
                PrintLane.add(id);
            }
        }
    }

    /**
     * Returns a List of all LaneIDs of the Simulation
     * @return LaneIds
     */
    public List<String> getLaneIds(){
        return LaneIds;
    }

    /**
     *
     * @param id
     * @return LaneModel
     */
    public LaneModel getLaneModel(String id){
        return lanes.get(id);
    }

    /**
     * Returns a List of LaneIDs that are to be printed in the rendered Map of the GUI
     * @return PrintLane
     */
    public List<String> getPrintLanes(){
        PrintLane.sort(Comparator.comparingInt(
                s -> Integer.parseInt(s.substring(1))));
        return PrintLane;
    }

    /**
     * Returns a List of LaneIDs that can function as Endpoints of Routes, as they are at the Edge of the Simulation Map
     * @return EndLanes
     */
    public List<String> getEndLanes(){
        return EndLanes;
    }

}
