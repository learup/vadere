package org.vadere.util.triangulation.adaptive.plots.qualities;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.util.geometry.mesh.gen.AFace;
import org.vadere.util.geometry.mesh.gen.AHalfEdge;
import org.vadere.util.geometry.mesh.gen.AMesh;
import org.vadere.util.geometry.mesh.gen.AVertex;
import org.vadere.util.geometry.mesh.inter.IMeshSupplier;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.triangulation.IPointConstructor;
import org.vadere.util.triangulation.adaptive.IDistanceFunction;
import org.vadere.util.triangulation.adaptive.IEdgeLengthFunction;
import org.vadere.util.triangulation.adaptive.MeshPoint;
import org.vadere.util.triangulation.adaptive.PSDistmesh;
import org.vadere.util.triangulation.adaptive.PSDistmeshPanel;
import org.vadere.util.triangulation.adaptive.PSMeshingPanel;
import org.vadere.util.triangulation.improver.PSMeshing;

import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

/**
 * @author Benedikt Zoennchen
 */
public class RunTimeCPU extends JFrame {

    private static final Logger log = LogManager.getLogger(RunTimeCPU.class);

	/**
	 * Each geometry is contained this bounding box.
	 */
	private static final VRectangle bbox = new VRectangle(-1.01, -1.01, 2.02, 2.02);
	private static final IEdgeLengthFunction uniformEdgeLength = p -> 1.0;
	private static final IPointConstructor<MeshPoint> pointConstructor = (x, y) -> new MeshPoint(x, y, false);
	private static final double initialEdgeLength = 1.5;


    private static void overallUniformRing() {
	    IMeshSupplier<MeshPoint, AVertex<MeshPoint>, AHalfEdge<MeshPoint>, AFace<MeshPoint>> supplier = () -> new AMesh<>(pointConstructor);
	    IDistanceFunction distanceFunc = p -> Math.abs(0.7 - Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY())) - 0.3;
	    List<VShape> obstacles = new ArrayList<>();

	    double initialEdgeLength = 1.5;
	    double minInitialEdgeLength = 0.03;

	    while (initialEdgeLength >= minInitialEdgeLength) {
		    PSMeshing<MeshPoint, AVertex<MeshPoint>, AHalfEdge<MeshPoint>, AFace<MeshPoint>> meshGenerator = new PSMeshing<>(
				    distanceFunc,
				    uniformEdgeLength,
				    initialEdgeLength,
				    bbox, obstacles,
				    supplier);

		    StopWatch overAllTime = new StopWatch();
		    overAllTime.start();
		    meshGenerator.generate();
		    overAllTime.stop();

		    log.info("#vertices: " + meshGenerator.getMesh().getVertices().size());
		    log.info("#edges: " + meshGenerator.getMesh().getEdges().size());
		    log.info("#faces: " + meshGenerator.getMesh().getFaces().size());
		    log.info("quality" + meshGenerator.getQuality());
		    log.info("overall time: " + overAllTime.getTime() + "[ms]");

		    PSMeshingPanel<MeshPoint, AVertex<MeshPoint>, AHalfEdge<MeshPoint>, AFace<MeshPoint>> distmeshPanel = new PSMeshingPanel(meshGenerator.getMesh(), f -> false, 1000, 800, bbox);
		    JFrame frame = distmeshPanel.display();
		    frame.setVisible(true);
		    frame.setTitle("uniformRing()");
		    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		    distmeshPanel.repaint();

		    initialEdgeLength = initialEdgeLength * 0.5;
	    }
	}

	private static void stepAdaptiveRingEikMesh(double startLen, double endLen, double stepLen) {
		IMeshSupplier<MeshPoint, AVertex<MeshPoint>, AHalfEdge<MeshPoint>, AFace<MeshPoint>> supplier = () -> new AMesh<>(pointConstructor);
		IDistanceFunction distanceFunc = p -> Math.abs(0.7 - Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY())) - 0.3;
		IEdgeLengthFunction adaptiveEdgeLength =  p -> 1.0 + Math.max(-distanceFunc.apply(p), 0) * 8.0;
		List<VShape> obstacles = new ArrayList<>();

		double initialEdgeLength = startLen;
		double minInitialEdgeLength = endLen;

		List<Integer> nVertices = new ArrayList<>();
		List<Long> runTimes = new ArrayList<>();
		List<Double> initlialEdgeLengths = new ArrayList<>();

		while (initialEdgeLength >= minInitialEdgeLength) {
			initlialEdgeLengths.add(initialEdgeLength);
			PSMeshing<MeshPoint, AVertex<MeshPoint>, AHalfEdge<MeshPoint>, AFace<MeshPoint>> meshGenerator = new PSMeshing<>(
					distanceFunc,
					adaptiveEdgeLength,
					initialEdgeLength,
					bbox, obstacles,
					supplier);

			StopWatch overAllTime = new StopWatch();

			int steps = 0;
			overAllTime.start();
			overAllTime.suspend();
			do {
				overAllTime.resume();
				meshGenerator.improve();
				overAllTime.suspend();
				steps++;
			} while (!meshGenerator.isFinished());

			log.info("#vertices: " + meshGenerator.getMesh().getVertices().size());
			log.info("#edges: " + meshGenerator.getMesh().getEdges().size());
			log.info("#faces: " + meshGenerator.getMesh().getFaces().size());
			log.info("quality: " + meshGenerator.getQuality());
			log.info("#step: " + steps);
			log.info("overall time: " + overAllTime.getTime() + "[ms]");
			log.info("step avg time: " + overAllTime.getTime() / steps + "[ms]");

			nVertices.add(meshGenerator.getMesh().getVertices().size());
			runTimes.add( overAllTime.getTime());

			/*PSMeshingPanel<MeshPoint, AVertex<MeshPoint>, AHalfEdge<MeshPoint>, AFace<MeshPoint>> distmeshPanel = new PSMeshingPanel(meshGenerator.getMesh(), f -> false, 1000, 800, bbox);
			JFrame frame = distmeshPanel.display();
			frame.setVisible(true);
			frame.setTitle("uniformRing()");
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			distmeshPanel.repaint();*/

			initialEdgeLength = initialEdgeLength * 0.5;

		}

		// 200 steps
		System.out.println("print overall runtimes for CPU");
		System.out.println("#vertices: [" + nVertices.stream().map(n -> n+"").reduce("", (s1,s2) -> s1 + "," + s2).substring(1) + "]");
		System.out.println("runtime in ms: [" + runTimes.stream().map(n -> n+"").reduce("", (s1,s2) -> s1 + "," + s2).substring(1) + "]");
		System.out.println("init edge lengths: [" + initlialEdgeLengths.stream().map(n -> n+"").reduce("", (s1,s2) -> s1 + "," + s2).substring(1) + "]");
	}

	private static void stepAdaptiveRingDistMesh(double startLen, double endLen, double stepLen) {
		IMeshSupplier<MeshPoint, AVertex<MeshPoint>, AHalfEdge<MeshPoint>, AFace<MeshPoint>> supplier = () -> new AMesh<>(pointConstructor);
		IDistanceFunction distanceFunc = p -> Math.abs(0.7 - Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY())) - 0.3;
		IEdgeLengthFunction adaptiveEdgeLength =  p -> 1.0 + Math.max(-distanceFunc.apply(p), 0) * 8.0;
		List<VShape> obstacles = new ArrayList<>();

		double initialEdgeLength = startLen;
		double minInitialEdgeLength = endLen;

		List<Integer> nVertices = new ArrayList<>();
		List<Long> runTimes = new ArrayList<>();
		List<Double> initlialEdgeLengths = new ArrayList<>();

		while (initialEdgeLength >= minInitialEdgeLength) {
			initlialEdgeLengths.add(initialEdgeLength);
			PSDistmesh meshGenerator = new PSDistmesh(distanceFunc,
					adaptiveEdgeLength,
					initialEdgeLength,
					bbox, obstacles);

			StopWatch overAllTime = new StopWatch();

			int steps = 0;
			overAllTime.start();
			overAllTime.suspend();
			do {
				overAllTime.resume();
				meshGenerator.step();
				overAllTime.suspend();
				steps++;
			} while (!meshGenerator.hasMaximalSteps());

			log.info("#vertices: " + meshGenerator.getPoints().size());
			log.info("quality: " + meshGenerator.getQuality());
			log.info("#step: " + steps);
			log.info("#tris: " + meshGenerator.getNumberOfReTriangulations());
			log.info("overall time: " + overAllTime.getTime() + "[ms]");
			log.info("step avg time: " + overAllTime.getTime() / steps + "[ms]");

			nVertices.add(meshGenerator.getPoints().size());
			runTimes.add( overAllTime.getTime());

			/*PSDistmeshPanel distmeshPanel = new PSDistmeshPanel(meshGenerator, 1000, 800, bbox, t -> false);
			JFrame frame = distmeshPanel.display();
			frame.setVisible(true);
			frame.setTitle("uniformRing()");
			frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			distmeshPanel.repaint();*/

			initialEdgeLength = initialEdgeLength * 0.5;

		}

		// 200 steps
		System.out.println("print overall runtimes for CPU");
		System.out.println("#vertices: [" + nVertices.stream().map(n -> n+"").reduce("", (s1,s2) -> s1 + "," + s2).substring(1) + "]");
		System.out.println("runtime in ms: [" + runTimes.stream().map(n -> n+"").reduce("", (s1,s2) -> s1 + "," + s2).substring(1) + "]");
		System.out.println("init edge lengths: [" + initlialEdgeLengths.stream().map(n -> n+"").reduce("", (s1,s2) -> s1 + "," + s2).substring(1) + "]");
	}

    public static void main(String[] args) {
		stepAdaptiveRingDistMesh(0.1, 0.005, 0.01);
	    //stepAdaptiveRingEikMesh(0.1, 0.005, 0.01);
    }
}