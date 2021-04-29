package org.vadere.simulator.control.external;

import org.json.JSONArray;
import org.json.JSONObject;


public class CtlCommand {

    JSONObject rawCommand;


    public CtlCommand(JSONObject command){

        rawCommand = command;

    };

    public String getModelName(){
        return rawCommand.getString("model");
    }

    public Double getExecTime(){
        return rawCommand.getDouble("time");
    }


    public JSONArray getPedCommand(){

        return rawCommand.getJSONArray("pedCommand");

    }




}
