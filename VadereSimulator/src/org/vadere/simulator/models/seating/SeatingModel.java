package org.vadere.simulator.models.seating;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.vadere.simulator.control.ActiveCallback;
import org.vadere.simulator.models.Model;
import org.vadere.state.attributes.Attributes;
import org.vadere.state.attributes.scenario.AttributesAgent;
import org.vadere.state.scenario.Topography;
import org.vadere.util.data.Table;

/**
 * This model can only be used with train scenarios complying with scenarios generated by Traingen.
 * 
 * To enable this model, add this model's class name to the main model's submodel list and
 * load a train topography.
 * 
 *
 */
public class SeatingModel implements ActiveCallback, Model {

	private final Logger log = Logger.getLogger(SeatingModel.class);

	private TrainModel trainModel;
	private Topography topography;

	@Override
	public Map<String, Table> getOutputTables() {
		return new HashMap<>();
	}

	@Override
	public void preLoop(double simTimeInSec) {
		// before simulation
	}

	@Override
	public void postLoop(double simTimeInSec) {
		// after simulation
	}

	@Override
	public void update(double simTimeInSec) {
		final Random r = new Random();
		final int seatCount = trainModel.getSeats().size();
		trainModel.getPedestrians().stream()
				.filter(p -> p.getTargets().isEmpty())
				.forEach(p -> p.getTargets().add(r.nextInt(seatCount)));
	}

	@Override
	public void initialize(List<Attributes> attributesList, Topography topography,
			AttributesAgent attributesPedestrian, Random random) {
		this.topography = topography;
		this.trainModel = new TrainModel(topography);
	}

}
