package org.vadere.util.triangulation.adaptive.plots;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.vadere.util.geometry.mesh.gen.AFace;
import org.vadere.util.geometry.mesh.gen.AHalfEdge;
import org.vadere.util.geometry.mesh.gen.AMesh;
import org.vadere.util.geometry.mesh.gen.AVertex;
import org.vadere.util.geometry.mesh.inter.IMeshSupplier;
import org.vadere.util.geometry.shapes.VPolygon;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.geometry.shapes.VShape;
import org.vadere.util.geometry.shapes.VTriangle;
import org.vadere.util.triangulation.adaptive.IDistanceFunction;
import org.vadere.util.triangulation.adaptive.IEdgeLengthFunction;
import org.vadere.util.triangulation.adaptive.MeshPoint;
import org.vadere.util.triangulation.adaptive.PSDistmesh;
import org.vadere.util.triangulation.adaptive.PSDistmeshPanel;
import org.vadere.util.triangulation.adaptive.Parameters;

import javax.swing.*;

import java.util.*;
import java.util.function.Predicate;

/**
 * @author Benedikt Zoennchen
 */
public class DistMeshTests extends JFrame {

	private static final Logger log = LogManager.getLogger(DistMeshTests.class);

	private static void testVisual()
    {
	    VRectangle bbox = new VRectangle(-1.01, -1.01, 2.02, 2.02);
	    //double initialEdgeLength = 0.07; // 0.1
	    double initialEdgeLength = 0.03; // 0.05

	    //IDistanceFunction distanceFunc = p -> Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY()) - 1.0;
	    //IDistanceFunction distanceFunc = p -> Math.abs(0.7 - Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY())) - 0.3;
	    VPolygon hex = VShape.generateHexagon(0.4);
	    IDistanceFunction quader = p -> Math.max(Math.abs(p.getX()), Math.abs(p.getY())) - 1.0;
	    IDistanceFunction circ = p -> Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY()) - 1.0;
	    IDistanceFunction distanceFunc = IDistanceFunction.intersect(quader, IDistanceFunction.create(bbox, hex));


	    IEdgeLengthFunction edgeLengthFunction = p -> 1.0 + Math.max(-distanceFunc.apply(p), 0) * 8.0;
	    List<VShape> obstacles = new ArrayList<>();
	    obstacles.add(hex);
	    obstacles.add(new VRectangle(-1,-1,2,2));


	    PSDistmesh meshGenerator = new PSDistmesh(distanceFunc,
			    p -> 1.0,
			    initialEdgeLength,
			    bbox, obstacles);

	    StopWatch overAllTime = new StopWatch();
	    overAllTime.start();

	    Predicate<VTriangle> predicate = t -> meshGenerator.getQuality(t) < 0.4;
	    PSDistmeshPanel distmeshPanel = new PSDistmeshPanel(meshGenerator, 1000, 800, bbox, predicate);

	    JFrame frame = distmeshPanel.display();
	    frame.setVisible(true);
	    frame.setTitle("DistMesh: uniformCircle("+ initialEdgeLength +")");
	    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

	    int step = 0;
	    log.debug("#vertices: " + meshGenerator.getPoints().size());
	    while (step < 300) {

		    meshGenerator.step();
		    step++;
		    try {
			    Thread.sleep(10);
		    } catch (InterruptedException e) {
			    e.printStackTrace();
		    }
		    distmeshPanel.repaint();
		    double quality = meshGenerator.getQuality();
		    log.debug("quality: " + quality);
		    log.debug("min-quality: " + meshGenerator.getMinQuality());
		    //log.debug("min-quality: " + meshGenerator.getMinQuality());
		    log.debug("step: " + step);
		    log.debug("#triangulations: " + meshGenerator.getNumberOfReTriangulations());
	    }
	    overAllTime.stop();
    }


    public static void main(String[] args) {
	    testVisual();
    }
}