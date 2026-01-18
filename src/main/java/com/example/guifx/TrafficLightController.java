package com.example.guifx;

import java.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.sumo.libtraci.Junction;
import org.eclipse.sumo.libtraci.TrafficLight;
import org.eclipse.sumo.libtraci.TraCIPosition;

/**
 * TrafficLightController is a Controller to control, track and update all Traffic Lights in the simulation
 */
public class TrafficLightController {
    private List<String> tlIds;
    private Map<String, TrafficLightModel> tlList;
    private static final Logger LOG = LogManager.getLogger(TrafficLightController.class.getName());

    /**
     * Constructs a TrafficLightController and initializes all Traffic Lights in form of instances of the TrafficLightModel class
     */
    public TrafficLightController() {
        this.tlList = new HashMap<>();

        tlIds = TrafficLight.getIDList();
        for (String id : tlIds) {
            TrafficLightModel tl = new TrafficLightModel(id, TrafficLight.getPhase(id),
                    TrafficLight.getRedYellowGreenState(id));
            tlList.put(tl.getId(), tl);
        }
        LOG.info("Scanned for Traffic Lights and found: " + tlList.size() + "!");
    }

    /**
     * Returns the HashMap, in which all Traffic Light Models are saved
     * 
     * @return Hashmap of traffic light IDs to TrafficLightModel
     */
    public Map<String, TrafficLightModel> getTlList() {
        return tlList;
    }

    /**
     * Returns the list of all Traffic Light IDs
     * 
     * @return List of traffic light IDs
     */
    public List<String> getTlIds() {
        return tlIds;
    }

    /**
     * Changes the Traffic Light Phase of a given TrafficLight Group (TL1 or TL2) with a newly given duration
     * @param duration
     * @param GroupID
     */
    public void changePhase(double duration, String GroupID) {
        for (String id : tlIds) {
            TrafficLightModel tl1 = tlList.get(id);
            if (tl1.getGroupID().equals(GroupID)) {
                int currentPhase = TrafficLight.getPhase(tl1.getId());
                if (currentPhase == 0) { // Phase 0 = green
                    tl1.setDuration(duration);

                    //To change phase duration persistently, changing the TL Logic of SUMO is necessary
                    var tllogic = TrafficLight.getAllProgramLogics(tl1.getId()).get(0);
                    tllogic.getPhases().get(2).setDuration(duration);
                    TrafficLight.setProgramLogic(tl1.getId(), tllogic);

                    tl1.setPhase(2); // Phase 2 = red
                    TrafficLight.setPhase(tl1.getId(), 2);

                } else if (currentPhase == 2) {
                    tl1.setDuration(duration);

                    var tllogic = TrafficLight.getAllProgramLogics(tl1.getId()).get(0);
                    tllogic.getPhases().get(0).setDuration(duration);
                    TrafficLight.setProgramLogic(tl1.getId(), tllogic);

                    tl1.setPhase(0);
                    TrafficLight.setPhase(tl1.getId(), 0);
                }
            }
        }
    }

    /**
     * Returns the time to the next Phase Switch of a given Traffic Light
     * @param TlId
     * @return
     */
    public double remainingTime(String TlId){
        if (tlList.containsKey(TlId)){
            return TrafficLight.getNextSwitch(TlId);
        }
        else {
            return -1;
        }
    }

    /**
     * Returns the position of a traffic light
     * 
     * @param tl TrafficLightModel
     * @return position of the traffic light
     */
    public TraCIPosition getTlPosition(TrafficLightModel tl) {
        return Junction.getPosition(tl.getId());
    }

    /**
     * This function is called every simulation step, to synchronize our local TrafficLightModel instances with the TrafficLight values, e.g. current Phase, inside of SUMO
     */
    public void updateTLModel(){
        for (String id : tlIds){
            TrafficLightModel tl = tlList.get(id);
            tl.setDuration(TrafficLight.getPhaseDuration(id));
            tl.setPhase(TrafficLight.getPhase(id));
            tl.setRedYellowGreenState(TrafficLight.getRedYellowGreenState(id));
        }
    }
}
