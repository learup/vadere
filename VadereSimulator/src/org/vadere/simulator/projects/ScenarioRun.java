package org.vadere.simulator.projects;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.simulator.control.PassiveCallback;
import org.vadere.simulator.control.Simulation;
import org.vadere.simulator.models.MainModel;
import org.vadere.simulator.models.MainModelBuilder;
import org.vadere.simulator.projects.dataprocessing.DataProcessingJsonManager;
import org.vadere.simulator.projects.dataprocessing.ProcessorManager;
import org.vadere.util.io.IOUtils;

/**
 * Manages single simulation runs.
 * 
 * @author Jakob Schöttl
 * 
 */
public class ScenarioRun implements Runnable {

	private static Logger logger = LogManager.getLogger(ScenarioRun.class);

	private Path outputPath;

	private final List<PassiveCallback> passiveCallbacks = new LinkedList<>();

	private final DataProcessingJsonManager dataProcessingJsonManager;

	private Simulation simulation;
	private ProcessorManager processorManager;

	private final Scenario scenario;
	private final ScenarioStore scenarioStore; // contained in scenario, but here for convenience

	private final RunnableFinishedListener finishedListener;

	public ScenarioRun(final Scenario scenario, RunnableFinishedListener scenarioFinishedListener) {
		this(scenario, IOUtils.OUTPUT_DIR, scenarioFinishedListener);
	}

	public ScenarioRun(final Scenario scenario, final String outputDir, final RunnableFinishedListener scenarioFinishedListener) {
		this.scenario = scenario;
		this.scenarioStore = scenario.getScenarioStore();
		this.dataProcessingJsonManager = scenario.getDataProcessingJsonManager();
		this.setOutputPaths(Paths.get(outputDir)); // TODO [priority=high] [task=bugfix] [Error?] this is a relative path. If you start the application via eclipse this will be VadereParent/output
		this.finishedListener = scenarioFinishedListener;
	}

	/**
	 * This method runs a simulation. It must not catch any exceptions! The
	 * caller (i.e. the calling thread) should catch exceptions and call
	 * {@link #simulationFailed(Throwable)}.
	 */
	@Override
	public void run() {
		try {
			logger.info(String.format("Initializing scenario. Start of scenario '%s'...", scenario.getName()));

			scenarioStore.topography.reset();

			// Watch out: apparently, there is a GUI bug. A GUI component (thread etc.) changes
			// "scenarioStore.topography" at some point in time. This causes that pedestrians overlap.
			// As workaround, clone original "topography" from "scenarioStore" and hand over the cloned
			// "topography".
            // But watch out: the original topography is still changed by the GUI component
            // and we must find out where and when!
			ScenarioStore storeClone = scenarioStore.clone();

			MainModelBuilder modelBuilder = new MainModelBuilder(storeClone);
			modelBuilder.createModelAndRandom();

			final MainModel mainModel = modelBuilder.getModel();
			final Random random = modelBuilder.getRandom();
			
			// prepare processors and simulation data writer
			processorManager = dataProcessingJsonManager.createProcessorManager(mainModel);

			// Only create output directory and write .scenario file if there is any output.
			if(!processorManager.isEmpty()) {
                createAndSetOutputDirectory();
                scenario.saveToOutputPath(outputPath);
            }

			sealAllAttributes();

			// Run simulation main loop from start time = 0 seconds
			simulation = new Simulation(mainModel, 0, storeClone.name, storeClone, passiveCallbacks, random, processorManager);
			simulation.run();

		} catch (Exception e) {
			throw new RuntimeException("Simulation failed.", e);
		} finally {
			doAfterSimulation();
		}
	}
	
	public void simulationFailed(Throwable e) {
			e.printStackTrace();
			logger.error(e);
	}

	protected void doAfterSimulation() {
		if (finishedListener != null)
			finishedListener.finished(this);

		logger.info(String.format("Simulation of scenario %s finished.", scenario.getName()));
	}

	public boolean isRunning() {
		return simulation != null && simulation.isRunning();
	}

	public void addPassiveCallback(final PassiveCallback pc) {
		passiveCallbacks.add(pc);
	}

	public void setOutputPaths(final Path outputPath) {
		if (dataProcessingJsonManager.isTimestamped()) {
			String dateString = new SimpleDateFormat(IOUtils.DATE_FORMAT).format(new Date());
			this.outputPath = Paths.get(outputPath.toString(), String.format("%s_%s", scenario.getName(), dateString));
		} else {
			this.outputPath = Paths.get(outputPath.toString(), scenario.getName());
		}
	}

	public void pause() {
		if (simulation != null) { // TODO throw an illegal state exception if simulation is not running
			simulation.pause();
		}
	}

	public void resume() {
		if (simulation != null) { // TODO throw an illegal state exception if simulation is not running
			simulation.resume();
		}
	}

	private void createAndSetOutputDirectory() {
		try {
			// Create output directory
			Files.createDirectories(outputPath);
			processorManager.setOutputPath(outputPath.toString());
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@Override
	public String toString() {
		return scenario.getName();
	}

	public String readyToRunResponse() { // TODO [priority=medium] [task=check] add more conditions
		if (scenarioStore.mainModel == null) {
			return scenarioStore.name + ": no mainModel is set";
		}
		return null;
	}

	public Scenario getScenario() {
		return scenario;
	}

	private void sealAllAttributes() {
		scenarioStore.sealAllAttributes();
		processorManager.sealAllAttributes();
	}

}
