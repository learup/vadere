package org.vadere.simulator.models.groups.sir;


import org.vadere.annotation.factories.models.ModelClass;
import org.vadere.simulator.models.Model;
import org.vadere.simulator.models.groups.AbstractGroupModel;
import org.vadere.simulator.models.groups.Group;
import org.vadere.simulator.models.groups.GroupSizeDeterminator;
import org.vadere.simulator.models.potential.fields.IPotentialFieldTarget;
import org.vadere.simulator.projects.Domain;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.models.AttributesSIRG;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.DynamicElementContainer;
import org.vadere.state.scenario.Pedestrian;
import org.vadere.state.scenario.Topography;
import org.vadere.util.geometry.LinkedCellsGrid;

import java.util.*;

/**
 * Implementation of groups for a susceptible / infected / removed (SIR) model.
 */
@ModelClass
public class SIRGroupModel extends AbstractGroupModel<SIRGroup> {

	private Random random;
	private LinkedHashMap<Integer, SIRGroup> groupsById;
	private Map<Integer, LinkedList<SIRGroup>> sourceNextGroups;
	private AttributesSIRG attributesSIRG;
	private Topography topography;
	private IPotentialFieldTarget potentialFieldTarget;
	private int totalInfected = 0;
	LinkedCellsGrid<Pedestrian> linkedCellsGrid;
	private double infectionRate;
	private double recoveryRate;


	public SIRGroupModel() {
		this.groupsById = new LinkedHashMap<>();
		this.sourceNextGroups = new HashMap<>();

	}

	@Override
	public void initialize(List<Attributes> attributesList, Domain domain,
	                       AttributesAgent attributesPedestrian, Random random) {
		this.attributesSIRG = Model.findAttributes(attributesList, AttributesSIRG.class);
		this.topography = domain.getTopography();
		this.random = random;
        this.totalInfected = 0;
        this.linkedCellsGrid = topography.getSpatialMap(Pedestrian.class);
        this.infectionRate = 1-Math.pow(1-attributesSIRG.getInfectionRate(),attributesSIRG.getSimTimeStepLength()/0.5);
		this.recoveryRate = 1-Math.pow(1-attributesSIRG.getRecoveryRate(),attributesSIRG.getSimTimeStepLength()/0.5);
	}

	@Override
	public void setPotentialFieldTarget(IPotentialFieldTarget potentialFieldTarget) {
		this.potentialFieldTarget = potentialFieldTarget;
		// update all existing groups
		for (SIRGroup group : groupsById.values()) {
			group.setPotentialFieldTarget(potentialFieldTarget);
		}
	}

	@Override
	public IPotentialFieldTarget getPotentialFieldTarget() {
		return potentialFieldTarget;
	}

	private int getFreeGroupId() {
		if(this.random.nextDouble() < attributesSIRG.getInfectionRate()
        || this.totalInfected < this.attributesSIRG.getInfectionsAtStart()) {
			if(!getGroupsById().containsKey(SIRType.ID_INFECTED.ordinal()))
			{
				SIRGroup g = getNewGroup(SIRType.ID_INFECTED.ordinal(), Integer.MAX_VALUE/2);
				getGroupsById().put(SIRType.ID_INFECTED.ordinal(), g);
			}
            this.totalInfected += 1;
			return SIRType.ID_INFECTED.ordinal();
		}
		else{
			if(!getGroupsById().containsKey(SIRType.ID_SUSCEPTIBLE.ordinal()))
			{
				SIRGroup g = getNewGroup(SIRType.ID_SUSCEPTIBLE.ordinal(), Integer.MAX_VALUE/2);
				getGroupsById().put(SIRType.ID_SUSCEPTIBLE.ordinal(), g);
			}
			return SIRType.ID_SUSCEPTIBLE.ordinal();
		}
	}


	@Override
	public void registerGroupSizeDeterminator(int sourceId, GroupSizeDeterminator gsD) {
		sourceNextGroups.put(sourceId, new LinkedList<>());
	}

	@Override
	public int nextGroupForSource(int sourceId) {
		return Integer.MAX_VALUE/2;
	}

	@Override
	public SIRGroup getGroup(final Pedestrian pedestrian) {
		SIRGroup group = groupsById.get(pedestrian.getGroupIds().getFirst());
		assert group != null : "No group found for pedestrian";
		return group;
	}

	@Override
	protected void registerMember(final Pedestrian ped, final SIRGroup group) {
		groupsById.putIfAbsent(ped.getGroupIds().getFirst(), group);
	}

	@Override
	public Map<Integer, SIRGroup> getGroupsById() {
		return groupsById;
	}

	@Override
	protected SIRGroup getNewGroup(final int size) {
		return getNewGroup(getFreeGroupId(), size);
	}

	@Override
	protected SIRGroup getNewGroup(final int id, final int size) {
		if(groupsById.containsKey(id))
		{
			return groupsById.get(id);
		}
		else
		{
			return new SIRGroup(id, this);
		}
	}

	private void initializeGroupsOfInitialPedestrians() {
		// get all pedestrians already in topography
		DynamicElementContainer<Pedestrian> c = topography.getPedestrianDynamicElements();

		if (c.getElements().size() > 0) {
			// TODO: fill in code to assign pedestrians in the scenario at the beginning (i.e., not created by a source)
            //  to INFECTED or SUSCEPTIBLE groups.
		}
	}

	protected void assignToGroup(Pedestrian ped, int groupId) {
		SIRGroup currentGroup = getNewGroup(groupId, Integer.MAX_VALUE/2);
		currentGroup.addMember(ped);
		ped.getGroupIds().clear();
		ped.getGroupSizes().clear();
		ped.addGroupId(currentGroup.getID(), currentGroup.getSize());
		registerMember(ped, currentGroup);
	}

	protected void assignToGroup(Pedestrian ped) {
		int groupId = getFreeGroupId();
		assignToGroup(ped, groupId);
	}


	/* DynamicElement Listeners */

	@Override
	public void elementAdded(Pedestrian pedestrian) {
		assignToGroup(pedestrian);
	}

	@Override
	public void elementRemoved(Pedestrian pedestrian) {
		Group group = groupsById.get(pedestrian.getGroupIds().getFirst());
		if (group.removeMember(pedestrian)) { // if true pedestrian was last member.
			groupsById.remove(group.getID());
		}
	}

	/* Model Interface */

	@Override
	public void preLoop(final double simTimeInSec) {
		initializeGroupsOfInitialPedestrians();
		topography.addElementAddedListener(Pedestrian.class, this);
		topography.addElementRemovedListener(Pedestrian.class, this);
	}

	@Override
	public void postLoop(final double simTimeInSec) {
	}

	@Override
	public void update(final double simTimeInSec) {
		// check the positions of all pedestrians and switch groups to INFECTED (or REMOVED).
		DynamicElementContainer<Pedestrian> c = topography.getPedestrianDynamicElements();
		// get the up-to-date topography information of pedestrians
		this.linkedCellsGrid = topography.getSpatialMap(Pedestrian.class);

		if (c.getElements().size() > 0) {
			//loop over all pedestrians
			for (Pedestrian p : c.getElements()) {
				SIRGroup g = getGroup(p);

				// loop over neighbors
				for (Pedestrian p_neighbor : linkedCellsGrid.getObjects(p.getPosition(), attributesSIRG.getInfectionMaxDistance())) {
					//skip, if neighbors is itself
					if (p == p_neighbor) continue;

					//if pedestrian belongs to susceptible group
					if (g.getID() == SIRType.ID_SUSCEPTIBLE.ordinal()) {
						if (getGroup(p_neighbor).getID() != SIRType.ID_INFECTED.ordinal()){
							continue;
						}
						if (this.random.nextDouble() < this.infectionRate) {
							elementRemoved(p);
							assignToGroup(p, SIRType.ID_INFECTED.ordinal());
						}
					}
					// if pedestrian belongs to infected group
					else if (g.getID() == SIRType.ID_INFECTED.ordinal()) {
						if (this.random.nextDouble() < this.recoveryRate) {
							elementRemoved(p);
							assignToGroup(p, SIRType.ID_RECOVERY.ordinal());
						}
					}
				}




			}
		}
	}
}