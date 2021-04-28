package org.vadere.simulator.control.psychology.cognition.models;

import com.fasterxml.jackson.databind.JsonNode;
import org.json.JSONObject;
import org.vadere.simulator.utils.topography.TopographyHelper;
import org.vadere.state.psychology.cognition.SelfCategory;
import org.vadere.state.psychology.perception.types.KnowledgeItem;
import org.vadere.state.psychology.perception.types.NavigationApp;
import org.vadere.state.psychology.perception.types.Stimulus;
import org.vadere.state.psychology.perception.types.Threat;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Topography;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class NavigationApplicationCognitionModel implements ICognitionModel {


    private Topography topography;

    @Override
    public void initialize(Topography topography) {
        this.topography = topography;
    }


    @Override
    public void update(final Collection<Pedestrian> pedestrians) {

    }


    public void update(final Collection<Pedestrian> pedestrians, JSONObject textmessage) {

        for (Pedestrian pedestrian : pedestrians) {
            if (pedestrian.getMostImportantStimulus() instanceof NavigationApp) {
                pedestrian.setTargets(revealNextTargetToPedestrian());
            }

        }

    }

    private LinkedList<Integer> revealNextTargetToPedestrian(){

        LinkedList<Integer> targetIDs  = new LinkedList<Integer>();
        targetIDs.add(2);
        return targetIDs;

    }




}
