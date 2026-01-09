package com.example.guifx;

import org.eclipse.sumo.libtraci.Lane;
import java.util.*;

public class LaneController {
    private List<String> LaneIds;
    private Map<String, LaneModel> lanes;
    private String[] notToPrint;
    private List<String> PrintLane;


    public LaneController() {
        this.LaneIds = Lane.getIDList();
        this.lanes = new HashMap<>();
        this.notToPrint = new String[]{"L2", "L22", "L25", "L57", "L54", "L29", "L27", "L55", "L56", "L19", "L20", "L35", "L43", "L10", "L12", "L13", "L36", "L37", "L41"};
        this.PrintLane = new ArrayList<>();
        for (String id : LaneIds){
            LaneModel lane = new LaneModel(id, Lane.getShape(id));
            lanes.put(id, lane);
            if ((!Arrays.asList(notToPrint).contains(id)) && (!id.contains("-")) && (id.contains("L"))){
                PrintLane.add(id);
            }
        }
    }

    public List<String> getLaneIds(){
        return LaneIds;
    }

    public LaneModel getLaneModel(String id){
        return lanes.get(id);
    }

    public List<String> getPrintLanes(){
       return PrintLane;
    }
}
