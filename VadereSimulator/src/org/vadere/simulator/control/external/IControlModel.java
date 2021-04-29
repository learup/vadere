package org.vadere.simulator.control.external;


import org.json.JSONArray;
import org.vadere.state.scenario.Topography;

import java.io.IOException;

public abstract class IControlModel {

    public CtlCommand rawCommand;
    public Topography topography;
    public Double simTime = -1.;


    public void addRawCommand(CtlCommand currentCommand){
        rawCommand = currentCommand;
    }

    public void update(Topography topography) {
        this.topography = topography;
        JSONArray command = rawCommand.getPedCommand();
        command.iterator().forEachRemaining(this::applyPedControl);
    }

    public abstract void applyPedControl(final Object o);


    public void update(Topography topography, Double time) throws IOException {
        if (Math.abs( rawCommand.getExecTime() - time) >= Double.MIN_VALUE){
            throw new IOException("Simtimestep does not match execution time of command.");
        }
        simTime = time;
        update(topography);
    }



}
