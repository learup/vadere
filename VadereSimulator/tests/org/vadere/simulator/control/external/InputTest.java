package org.vadere.simulator.control.external;


import org.jcodec.common.DictionaryCompressor;
import org.json.JSONObject;
import org.junit.Test;
import org.mockito.Mockito;
import org.vadere.simulator.context.VadereContext;
import org.vadere.simulator.control.simulation.SimulationState;
import org.vadere.simulator.projects.dataprocessing.datakey.TimestepPedestrianIdKey;
import org.vadere.simulator.projects.dataprocessing.processor.SimulationStateMock;
import org.vadere.simulator.utils.PedestrianListBuilder;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.psychology.cognition.SelfCategory;
import org.vadere.state.psychology.perception.types.ElapsedTime;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.state.simulation.FootStep;
import org.vadere.state.simulation.VTrajectory;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.io.IOUtils;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertTrue;


public class InputTest {






    @Test
    private void getModel() {

        String dataPath = "testResources/control/external/CorridorChoiceData.json";
        String data = "";

        try {
            data = IOUtils.readTextFile(dataPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        IControlModel ctlmodel = Input.getModel(data);


    }





}
