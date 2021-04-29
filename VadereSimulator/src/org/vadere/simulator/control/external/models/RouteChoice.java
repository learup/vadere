package org.vadere.simulator.control.external.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.jcodec.common.DictionaryCompressor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.vadere.simulator.control.external.IControlModel;
import org.vadere.state.scenario.Agent;
import org.vadere.state.scenario.Pedestrian;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Stream;

public class RouteChoice extends IControlModel {

    JSONObject command;



    public void applyPedControl(final Object pedCommand) {

        command = ((JSONObject) pedCommand);
        if(getEndTimeCmdValid() >= rawCommand.getExecTime()) {

            Collection<Pedestrian> peds = topography.getPedestrianDynamicElements().getElements();

            if (peds.stream().map(Pedestrian::getId).anyMatch(n-> n == getPedId()))
            {
                Pedestrian ped = topography.getPedestrianDynamicElements().getElement(getPedId());
                LinkedList<Integer> newTarget = displayTarget();
                ped.setTargets(newTarget);
            }

        }





    }

    private LinkedList<Integer> displayTarget() {

        LinkedList<Integer> possibleTargets = getTarget();
        LinkedList<Double> probs = getProbabilites();

        //TODO  draw from probs

        
        return possibleTargets;

    }

    private LinkedList<Integer> getTarget() {
        LinkedList<Integer> targets = new LinkedList<>();
        JSONArray targetList = (JSONArray) command.get("targetIds");

        for (Object t :targetList){
            targets.add((Integer) t);
        }
        return targets;
    }

    private LinkedList<Double> getProbabilites() {
        LinkedList<Double> probs = new LinkedList<>();
        JSONArray targetList = (JSONArray) command.get("probability");

        for (Object t :targetList){
            probs.add(((BigDecimal) t).doubleValue());
        }
        return probs;
    }


    private Double getEndTimeCmdValid(){
        return command.getDouble("endtime");
    }

    private int getPedId(){
        return command.getInt("id");
    }


}
