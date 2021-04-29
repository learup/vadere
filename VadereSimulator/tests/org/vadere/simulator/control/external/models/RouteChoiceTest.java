package org.vadere.simulator.control.external.models;


import org.json.JSONObject;
import org.junit.Test;
import org.vadere.simulator.control.external.CtlCommand;
import org.vadere.simulator.control.external.IControlModel;
import org.vadere.simulator.control.external.Input;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.psychology.cognition.SelfCategory;
import org.vadere.state.psychology.perception.types.ElapsedTime;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.io.IOUtils;

import java.io.IOException;
import java.util.*;


public class RouteChoiceTest {


    private List<Pedestrian> createPedestrians(int totalPedestrians) {
        LinkedList<Integer> initialTarget = new LinkedList<>();
        initialTarget.add(3);

        List<Pedestrian> pedestrians = new ArrayList<>();

        for (int i = 0; i < totalPedestrians; i++) {
            long seed = 0;
            Random random = new Random(seed);
            AttributesAgent attributesAgent = new AttributesAgent(i);

            Pedestrian currentPedestrian = new Pedestrian(attributesAgent, random);

            currentPedestrian.setMostImportantStimulus(new ElapsedTime());
            currentPedestrian.setSelfCategory(SelfCategory.TARGET_ORIENTED);

            currentPedestrian.setTargets(initialTarget);

            pedestrians.add(currentPedestrian);
        }

        return pedestrians;
    }

    private Topography createTopography(List<Pedestrian> initialPedestrians) {
        Topography topography = new Topography();

        initialPedestrians.stream().forEach(pedestrian -> topography.addElement(pedestrian));

        List<Target> targets = createTwoTargets();
        targets.stream().forEach(target -> topography.addTarget(target));

        return topography;
    }

    private ArrayList<Target> createTwoTargets() {
        ArrayList<Target> targets = new ArrayList<>();

        Target target1 = createTarget(new VPoint(0, 0), 1, 0);
        Target target2 = createTarget(new VPoint(5, 0), 1, 1);
        Target target3 = createTarget(new VPoint(10, 0), 1, 1);


        targets.add(target1);
        targets.add(target2);
        targets.add(target3);

        return targets;
    }

    private Target createTarget(VPoint center, double radius, int id) {
        VShape shape = new VCircle(center, radius);
        boolean absorbing = true;

        AttributesTarget attributesTarget = new AttributesTarget(shape, id, absorbing);
        Target target = new Target(attributesTarget);

        return target;
    }


    private RouteChoice getModel() {

        String dataPath = "testResources/control/external/CorridorChoiceData.json";
        String data = "";

        try {
            data = IOUtils.readTextFile(dataPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        RouteChoice routeChoice = new RouteChoice();
        routeChoice.addRawCommand(new CtlCommand(new JSONObject(data)));

        return routeChoice;
    }

    @Test
    public void updateState() throws IOException {

        RouteChoice routeChoice = getModel();
        Topography topo = createTopography(createPedestrians(2));
        routeChoice.update(topo, 0.4);


        Collection<Pedestrian> peds = topo.getPedestrianDynamicElements().getElements();

        for (Pedestrian p : peds){
            p.getId();
        }


    }
}
