package com.example.guifx;

import java.util.*;

import org.eclipse.sumo.libtraci.Junction;
import org.eclipse.sumo.libtraci.TraCIPosition;
import org.eclipse.sumo.libtraci.TrafficLight;

 /**
 * TrafficLightController is a class to control TrafficLights in the simulation
 */

public class TrafficLightController {

    private List<String> tlIds;
    private Map<String, TrafficLightModel> tlList;

    public TrafficLightController() {
        this.tlList = new HashMap<>();

        //Getting all predefined Traffic Lights
        tlIds = TrafficLight.getIDList();
        for (String id : tlIds)
        {
            TrafficLightModel tl = new TrafficLightModel(id, TrafficLight.getPhase(id), TrafficLight.getRedYellowGreenState(id));
            tlList.put(tl.getId(), tl);
        }
        System.out.println("Scanned for Traffic Lights and found: " + tlList.size() + "!");
    }

    public Map<String, TrafficLightModel> getTlList() {
        return tlList;
    }

    public List<String> getTlIds(){
        return tlIds;
    }

    public void changePhase(){
        TrafficLightModel tl1 = tlList.get("n2"); //--> Currently only Traffic Light we have.
        int currentPhase = tl1.getPhase();
        if (currentPhase == 0) { //Phase 0 = green
            tl1.setPhase(2); //Phase 2 == red
            TrafficLight.setPhase(tl1.getId(), 2);
        }
        else if (currentPhase == 2)
        {
            tl1.setPhase(0);
            TrafficLight.setPhase(tl1.getId(), 0);
        }
    }

    //TraCIPosition might also be an own class (ToDo for later)
    public TraCIPosition getTlPosition(TrafficLightModel tl){
        return Junction.getPosition(tl.getId());
        //tlController.getTlPosition(tlController.getTlList().get("n2")); --> Possible Calling Method e.g. from TrafficLightLayer (it needs the TrafficLightController)
    }
}
