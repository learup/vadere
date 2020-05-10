package org.vadere.simulator.projects.dataprocessing.datakey;

import org.jetbrains.annotations.NotNull;
import org.vadere.simulator.projects.dataprocessing.outputfile.TimestepPedestrianIdOverlapOutputFile;
import org.vadere.simulator.projects.dataprocessing.outputfile.TimestepPedestriansNearbyIdOutputFile;
import org.vadere.state.attributes.AttributesSimulation;

import java.util.Objects;

@OutputFileMap(outputFileClass = TimestepPedestriansNearbyIdOutputFile.class)
public class TimestepPedestriansNearbyIdKey implements DataKey<TimestepPedestriansNearbyIdKey> {

	private final int timeStep;
	private final int pedId1;	//smaller id
	private final int pedId2;	//bigger id
	private final boolean printForPostVis;
	private static boolean staticPrintForPostVis;


	public TimestepPedestriansNearbyIdKey(int timeStep, int pedA, int pedB, boolean printForPostVis) {
		this.timeStep = timeStep;
		this.pedId1 = Math.min(pedA, pedB);
		this.pedId2 = Math.max(pedA, pedB);
		this.printForPostVis = printForPostVis; // is an ugly one time thing that shouldn't be merged
		staticPrintForPostVis = printForPostVis;
	}


	public static String[] getHeaders(){
		//return new String[]{TimestepKey.getHeader(), PedestrianIdKey.getHeader(), "pedestrianNearbyId"};
		return new String[]{"pedestrianId", "simTime"};
	}

	public int getTimeStep() {
		return timeStep;
	}

	public int getPedId1() {
		return pedId1;
	}

	public int getPedId2() {
		return pedId2;
	}

	public String[] toStrings(){
		// printForPostVis is an ugly one time thing that shouldn't be merged
		if (printForPostVis) {
			//return new String[]{Integer.toString(999), Double.toString(timeStep*0.4), Double.toString(timeStep*0.4 + 0.4)};
			return new String[]{};
		}
		return new String[]{Integer.toString(timeStep), Integer.toString(pedId1), Integer.toString(pedId2)};
	}

	public boolean isContinuationOf(PedestriansNearbyData other, int toleranceTimesteps) {
		return other.getPedId1() == getPedId1() &&
				other.getPedId2() == getPedId2() &&
				(other.getStartTimestep() + other.getDurationTimesteps() <= getTimeStep()) &&
				(other.getStartTimestep() + other.getDurationTimesteps() + toleranceTimesteps >= getTimeStep());
	}
	public boolean isAccountedForBy(PedestriansNearbyData other) {
		return (other.getPedId1() == getPedId1() &&
				other.getPedId2() == getPedId2() &&
				getTimeStep() >= other.getStartTimestep() &&
				getTimeStep() < other.getStartTimestep() + other.getDurationTimesteps());
	}

	@Override
	public int compareTo(@NotNull TimestepPedestriansNearbyIdKey o) {
		int result = Integer.compare(this.timeStep, o.timeStep);
		if (result == 0) {
			result =  Integer.compare(this.pedId1, o.pedId1);
			if (result == 0){
				result = Integer.compare(this.pedId2, o.pedId2);
			}
		}
		return result;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		TimestepPedestriansNearbyIdKey that = (TimestepPedestriansNearbyIdKey) o;
		return timeStep == that.timeStep &&
				((pedId1 == that.pedId1 &&
						pedId2 == that.pedId2) || (pedId2 == that.pedId1 &&
						pedId1 == that.pedId2));
	}

	@Override
	public int hashCode() {

		return Objects.hash(timeStep, pedId1, pedId2);
	}

	@Override
	public String toString() {
		return "TimestepPedestrianIdOverlapKey{" +
				"timeStep=" + timeStep +
				", pedId1=" + pedId1 +
				", pedId2=" + pedId2 +
				'}';
	}


}
