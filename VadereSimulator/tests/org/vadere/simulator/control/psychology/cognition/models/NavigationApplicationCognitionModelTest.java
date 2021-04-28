package org.vadere.simulator.control.psychology.cognition.models;


import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.util.JSONPObject;
import netscape.javascript.JSObject;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.vadere.simulator.projects.io.JsonConverter;
import org.vadere.state.attributes.scenario.AttributesAgent;

import org.vadere.state.attributes.scenario.AttributesTarget;
import org.vadere.state.psychology.cognition.SelfCategory;
import org.vadere.state.psychology.perception.types.ElapsedTime;
import org.vadere.state.psychology.perception.types.NavigationApp;
import org.vadere.state.psychology.perception.types.Threat;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Target;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.shapes.VCircle;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;


import java.lang.reflect.Field;
import java.util.*;

import static org.junit.Assert.*;


public class NavigationApplicationCognitionModelTest {

    private List<Pedestrian> pedestrians;
    private Topography topography;

    @Before
    public void initializePedestrian() {
        AttributesAgent attributesAgent = new AttributesAgent(1);

        Pedestrian pedestrian = new Pedestrian(attributesAgent, new Random());

        this.pedestrians = List.of(pedestrian);
    }


    private List<Pedestrian> createPedestrians(int totalPedestrians, boolean usePedIdAsTargetId) {
        List<Pedestrian> pedestrians = new ArrayList<>();

        for (int i = 0; i < totalPedestrians; i++) {
            long seed = 0;
            Random random = new Random(seed);
            AttributesAgent attributesAgent = new AttributesAgent(i);

            Pedestrian currentPedestrian = new Pedestrian(attributesAgent, random);

            currentPedestrian.setMostImportantStimulus(new NavigationApp());
            currentPedestrian.setSelfCategory(SelfCategory.INFORMED);

            LinkedList<Integer> targetIds = (usePedIdAsTargetId) ? new LinkedList<>(Arrays.asList(i)) : new LinkedList<>();
            currentPedestrian.setTargets(targetIds);

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

        targets.add(target1);
        targets.add(target2);

        return targets;
    }

    private Target createTarget(VPoint center, double radius, int id) {
        VShape shape = new VCircle(center, radius);
        boolean absorbing = true;

        AttributesTarget attributesTarget = new AttributesTarget(shape, id, absorbing);
        Target target = new Target(attributesTarget);

        return target;
    }



    @Test
    public void getInformation() {

        List<Pedestrian> pedestrians = createPedestrians(2, false);
        Topography topography = createTopography(pedestrians);
        NavigationApplicationCognitionModel modelUnderTest = new NavigationApplicationCognitionModel();
        modelUnderTest.initialize(topography);


        NavigationApp navigationAppStimulus = new NavigationApp();
        modelUnderTest.update(pedestrians);


        for (Pedestrian pedestrian : pedestrians) {
            assertEquals( pedestrian.getKnowledgeBase().getKnowledge().size() , 0); // no information
            assertEquals( pedestrian.getTargets().size(),0); // no target
        }

        String info = "{\"targets\":\"[1,2]\",\"probabilities\":\"[0,1.0]\"}";
        JSONObject jsonObject = new JSONObject(info);



        List<Pedestrian> ped2 = new ArrayList<>();
        Pedestrian p = pedestrians.get(1);
        ped2.add(p);

        modelUnderTest.update(ped2, jsonObject);

        assertEquals( pedestrians.get(0).getTargets().size() , 0); // no information
        assertEquals( pedestrians.get(1).getTargets().size(),1); // no target
        

    }


}
