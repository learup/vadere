package org.vadere.simulator.projects.dataprocessing.processor;

import org.mockito.Mockito;
import org.vadere.simulator.control.simulation.SimulationState;
import org.vadere.state.scenario.Topography;
import org.vadere.util.test.MockProvider;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Abstract Class for {@link SimulationStateMock}s
 *
 * @author Stefan Schuhbäck
 */
public abstract class SimulationStateMock implements MockProvider<SimulationState> {

	protected SimulationState state;

	public SimulationStateMock() {
		this.state = mock(SimulationState.class, Mockito.RETURNS_DEEP_STUBS);
		mockIt();
	}

	SimulationStateMock(int simStep) {
		this.state = mock(SimulationState.class, Mockito.RETURNS_DEEP_STUBS);
		when(state.getStep()).thenReturn(simStep);
		mockIt();
	}

	public SimulationStateMock(Topography topography) {
		this.state = mock(SimulationState.class, Mockito.RETURNS_DEEP_STUBS);
		when(state.getTopography()).thenReturn(topography);
		mockIt();
	}

	@Override
	public SimulationState get() {
		return state;
	}
}
