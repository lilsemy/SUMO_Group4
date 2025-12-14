package com.example.guifx;

import java.util.*;

import org.eclipse.sumo.libtraci.Junction;
import org.eclipse.sumo.libtraci.TrafficLight;
import org.eclipse.sumo.libtraci.TraCIPosition;

/**
 * TrafficLightController controls traffic lights in the simulation
 */
public class TrafficLightController {

    private List<String> tlIds;
    private Map<String, TrafficLightModel> tlList;

    /**
     * Constructs a TrafficLightController and initializes all traffic lights
     */
    public TrafficLightController() {
        this.tlList = new HashMap<>();

        // Getting all predefined Traffic Lights
        tlIds = TrafficLight.getIDList();
        for (String id : tlIds) {
            TrafficLightModel tl = new TrafficLightModel(id, TrafficLight.getPhase(id),
                    TrafficLight.getRedYellowGreenState(id));
            tlList.put(tl.getId(), tl);
        }
        System.out.println("Scanned for Traffic Lights and found: " + tlList.size() + "!");
    }

    /**
     * Returns the map of traffic lights
     * 
     * @return Map of traffic light IDs to TrafficLightModel
     */
    public Map<String, TrafficLightModel> getTlList() {
        return tlList;
    }

    /**
     * Returns the list of traffic light IDs
     * 
     * @return List of traffic light IDs
     */
    public List<String> getTlIds() {
        return tlIds;
    }

    /**
     * Changes the phase of all traffic lights (green ↔ red)
     */
    public void changePhase() {
        for (String id : tlIds) {
            TrafficLightModel tl1 = tlList.get(id);
            int currentPhase = tl1.getPhase();
            if (currentPhase == 0) { // Phase 0 = green
                tl1.setPhase(2); // Phase 2 = red
                TrafficLight.setPhase(tl1.getId(), 2);
            } else if (currentPhase == 2) {
                tl1.setPhase(0);
                TrafficLight.setPhase(tl1.getId(), 0);
            }
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
}
