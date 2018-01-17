package org.vadere.util.triangulation.adaptive;

import org.vadere.util.geometry.mesh.gen.AFace;
import org.vadere.util.geometry.mesh.gen.PFace;
import org.vadere.util.geometry.shapes.VRectangle;
import org.vadere.util.geometry.shapes.VTriangle;

import javax.swing.*;
import java.util.ArrayList;
import java.util.function.Predicate;

/**
 * Created by Matimati-ka on 27.09.2016.
 */
public class TestEnhancedVersion4 extends JFrame {

    private TestEnhancedVersion4() {

		//IDistanceFunction distanceFunc1 = p -> 2 - Math.sqrt((p.getX()-1) * (p.getX()-1) + p.getY() * p.getY());
		//IDistanceFunction distanceFunc3 = p -> 2 - Math.sqrt((p.getX()-5) * (p.getX()-5) + p.getY() * p.getY());
		//IDistanceFunction distanceFunc = p -> -10+Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY());
		//IDistanceFunction distanceFunc = p -> 2 - Math.max(Math.abs(p.getX()-3), Math.abs(p.getY()));
		IDistanceFunction distanceFunc = p -> Math.abs(6 - Math.sqrt(p.getX() * p.getX() + p.getY() * p.getY())) - 4;
		//IDistanceFunction distanceFunc4 = p -> Math.max(Math.abs(p.getY()) - 4, Math.abs(p.getX()) - 25);
		//IEdgeLengthFunction edgeLengthFunc = p -> 1.0 + p.distanceToOrigin();
		IEdgeLengthFunction edgeLengthFunc = p -> 1.0;

		//IDistanceFunction distanceFunc = p -> Math.max(Math.max(Math.max(distanceFunc1.apply(p), distanceFunc2.apply(p)), distanceFunc3.apply(p)), distanceFunc4.apply(p));
		//IEdgeLengthFunction edgeLengthFunc = p -> 1.0 + Math.abs(distanceFunc.apply(p))/2;
		//IEdgeLengthFunction edgeLengthFunc = p -> 1.0 + p.distanceToOrigin();
		//IEdgeLengthFunction edgeLengthFunc = p -> 1.0 + Math.min(Math.abs(distanceFunc.apply(p) + 4), Math.abs(distanceFunc.apply(p)));
		//IEdgeLengthFunction edgeLengthFunc = p -> 1.0;
		VRectangle bbox = new VRectangle(-11, -11, 22, 22);
		CLPSMeshing meshGenerator = new CLPSMeshing(distanceFunc, edgeLengthFunc, 0.5, bbox, new ArrayList<>());
		meshGenerator.initialize();

        Predicate<VTriangle> predicate = triangle -> triangle.isNonAcute();
		PSMeshingPanel distmeshPanel = new PSMeshingPanel(meshGenerator, predicate, 1000, 800);
		JFrame frame = distmeshPanel.display();
		frame.setVisible(true);


		//System.out.print(TexGraphGenerator.meshToGraph(meshGenerator.getMesh()));
		//double maxLen = meshGenerator.step();
		double avgQuality = 0.0;
		long obscuteTriangles = -1;
		int counter = 0;
		long time = 0;
		int numberOfRetriangulations = 0;
		long triangulationTime = 0;

		while (counter < 1000) {

			/*for(int i = 0; i < 100 && !priorityQueue.isEmpty(); i++) {
				PFace<MeshPoint> face = priorityQueue.poll();
				System.out.println("lowest quality ("+counter+"):"+ meshGenerator.faceToQuality(face));
			}*/

			long ms = System.currentTimeMillis();
			//meshGenerator.refresh();
			//meshGenerator.retriangulate();
			boolean retriangulation = meshGenerator.step(true);
			if (retriangulation) {
				long tms = System.currentTimeMillis();
				//?meshGenerator.refresh();
				triangulationTime += System.currentTimeMillis() - tms;
				numberOfRetriangulations++;
			}
			ms = System.currentTimeMillis() - ms;			/*if(meshGenerator.step(true)) {
				meshGenerator.refresh();
				meshGenerator.retriangulate();
			}*/


			time += ms;
			System.out.println("Step-Time: " + ms);


			distmeshPanel.update();
			distmeshPanel.repaint();


			/*if(counter < 50) {
				meshGenerator.retriangulate();
			}*/
			counter++;
			/*try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}
		meshGenerator.finish();
		distmeshPanel.update();
		distmeshPanel.repaint();
		System.out.println("overall time: " + time);
		System.out.println("#retriangulations: " + numberOfRetriangulations);
		System.out.println("triangulation-time: " + triangulationTime);
		//System.out.print("finished:" + meshGenerator.getMesh().getVertices().stream().filter(v -> !meshGenerator.getMesh().isDestroyed(v)).count());
		//System.out.print("finished:" + avgQuality);
		//System.out.print(TexGraphGenerator.meshToGraph(meshGenerator.getMesh()));
		//if(counter == 1) {
		//
		//}
	}

    public static void main(String[] args) {
        new TestEnhancedVersion4();
    }
}