package org.vadere.simulator.control.external;

import org.json.JSONArray;

public interface IControlModel {


    void update();
    void update(Double simTimeSec);

    void addRawCommand(CtlCommand currentCommand);
}
