package org.vadere.simulator.control;

import org.vadere.state.scenario.*;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.logging.Logger;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Change target id of an agent which enters the corresponding {@link TargetChanger} area.
 *
 * {@link TargetChanger}'s attributes contain two important parameters to control the changing behavior:
 * <ul>
 *     <li>
 *         "changeTargetProbability": This defines how many percent of the agents,
 *         who enter the area, should change their target.
 *     </li>
 *     <li>
 *         If "nextTargetIsPedestrian == false", assign a new static target.
 *         Otherwise, randomly choose a pedestrian (with given target id) to follow.
 *     </li>
 * </ul>
 */
public class TargetChangerController {

    // Variables
    private static final Logger log = Logger.getLogger(TargetChangerController.class);

    public final TargetChanger targetChanger;
    private Topography topography;
    private Map<Integer, Agent> processedAgents;

    // Constructors
    public TargetChangerController(Topography topography, TargetChanger targetChanger) {
        this.targetChanger = targetChanger;
        this.topography = topography;
        this.processedAgents = new HashMap<>();
    }

    // Public Methods
    public void update(double simTimeInSec) {
        for (DynamicElement element : getDynamicElementsNearTargetChangerArea()) {

            final Agent agent;
            if (element instanceof Agent) {
                agent = (Agent) element;
            } else {
                log.error("The given object is not a subtype of Agent.");
                continue;
            }

            if (hasAgentReachedTargetChangerArea(agent) && processedAgents.containsKey(agent.getId()) == false) {
                logEnteringTimeOfAgent(agent, simTimeInSec);

                // TODO: First, use Binomial distribution to decide
                //   if target should be changed.
                boolean changeTarget = true;

                if (changeTarget) {
                    if (targetChanger.getAttributes().isNextTargetIsPedestrian()) {
                        useDynamicTargetForAgentOrUseStaticAsFallback(agent);
                    } else {
                        useStaticTargetForAgent(agent);
                    }
                }

                notifyListenersTargetChangerAreaReached(agent);

                processedAgents.put(agent.getId(), agent);
            }
        }
    }

    private Collection<DynamicElement> getDynamicElementsNearTargetChangerArea() {
        final Rectangle2D areaBounds = targetChanger.getShape().getBounds2D();
        final VPoint areaCenter = new VPoint(areaBounds.getCenterX(), areaBounds.getCenterY());

        final double reachDistance = targetChanger.getAttributes().getReachDistance();
        final double reachRadius = Math.max(areaBounds.getHeight(), areaBounds.getWidth()) + reachDistance;

        final Collection<DynamicElement> elementsNearArea = new LinkedList<>();

        List<Pedestrian> pedestriansNearArea = topography.getSpatialMap(Pedestrian.class).getObjects(areaCenter, reachRadius);
        elementsNearArea.addAll(pedestriansNearArea);

        return elementsNearArea;
    }

    private boolean hasAgentReachedTargetChangerArea(Agent agent) {
        final double reachDistance = targetChanger.getAttributes().getReachDistance();
        final VPoint agentPosition = agent.getPosition();
        final VShape targetChangerShape = targetChanger.getShape();

        return targetChangerShape.contains(agentPosition)
                || targetChangerShape.distance(agentPosition) < reachDistance;
    }

    private void logEnteringTimeOfAgent(Agent agent, double simTimeInSec) {
        Map<Integer, Double> enteringTimes = targetChanger.getEnteringTimes();
        Integer agentId = agent.getId();

        if (enteringTimes.containsKey(agentId) == false) {
            enteringTimes.put(agentId, simTimeInSec);
        }
    }

    private void useDynamicTargetForAgentOrUseStaticAsFallback(Agent agent) {
        int nextTarget = targetChanger.getAttributes().getNextTarget();

        Collection<Pedestrian> allPedestrians = topography.getElements(Pedestrian.class);
        List<Pedestrian> pedsWithCorrectTargetId = allPedestrians.stream()
                .filter(pedestrian -> pedestrian.getTargets().contains(nextTarget))
                .collect(Collectors.toList());

        if (pedsWithCorrectTargetId.size() > 0) {
            // Maybe, choose randomly in the long run.
            Pedestrian pedToFollow = pedsWithCorrectTargetId.get(0);
            agentFollowsOtherPedestrian(agent, pedToFollow);
        } else {
            useStaticTargetForAgent(agent);
        }
    }

    private void agentFollowsOtherPedestrian(Agent agent, Pedestrian pedToFollow) {
        // Create necessary target wrapper object.
        // Watch out: The main simulation loop creates the necessary
        // "TargetController" object in the next simulation step.
        TargetPedestrian targetPedestrian = new TargetPedestrian(pedToFollow);
        topography.addTarget(targetPedestrian);

        // Make "agent" a follower of "pedToFollow".
        LinkedList<Integer> nextTargetAsList = new LinkedList<>();
        nextTargetAsList.add(pedToFollow.getId());
        agent.setTargets(nextTargetAsList);
    }

    private void useStaticTargetForAgent(Agent agent) {
        LinkedList<Integer> newTarget = new LinkedList<>();
        newTarget.add(targetChanger.getAttributes().getNextTarget());
        agent.setTargets(newTarget);
    }

    private void notifyListenersTargetChangerAreaReached(final Agent agent) {
        for (TargetChangerListener listener : targetChanger.getTargetChangerListeners()) {
            listener.reachedTargetChanger(targetChanger, agent);
        }
    }

}