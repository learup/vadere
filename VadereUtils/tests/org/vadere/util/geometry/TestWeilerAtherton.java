package org.vadere.util.geometry;

import com.google.common.collect.Iterables;

import org.apache.commons.math3.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.vadere.util.geometry.mesh.gen.PFace;
import org.vadere.util.geometry.mesh.gen.PMesh;
import org.vadere.util.geometry.shapes.VPoint;
import org.vadere.util.geometry.shapes.VPolygon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestWeilerAtherton {

	@Before
	public void setUp() throws Exception {}


	@Test
	public void testNoIntersection() {
		VPolygon poly1 = GeometryUtils.toPolygon(new VPoint(0, 0), new VPoint(1, 1), new VPoint(1, -1));
		VPolygon poly2 = GeometryUtils.toPolygon(new VPoint(-0.01, 0), new VPoint(-1, 1), new VPoint(-1, -1));

		WeilerAtherton weilerAtherton = new WeilerAtherton(poly1, poly2);
		List<VPolygon> polygonList = weilerAtherton.execute();

		assertTrue(polygonList.contains(poly1));
		assertTrue(polygonList.contains(poly2));
		assertEquals(2, polygonList.size());
	}

	@Test
	public void testIntersectionFaceConstructionNoIntersections() {
		VPolygon poly1 = GeometryUtils.toPolygon(new VPoint(0, 0), new VPoint(1, 1), new VPoint(1, -1));
		VPolygon poly2 = GeometryUtils.toPolygon(new VPoint(-0.01, 0), new VPoint(-1, 1), new VPoint(-1, -1));

		WeilerAtherton weilerAtherton = new WeilerAtherton(poly1, poly2);
		Pair<PFace<WeilerAtherton.WeilerPoint>, PFace<WeilerAtherton.WeilerPoint>> pair = weilerAtherton.constructIntersectionFaces();

		PFace<WeilerAtherton.WeilerPoint> face1 = pair.getFirst();
		PFace<WeilerAtherton.WeilerPoint> face2 = pair.getSecond();

		// we need a mesh to iterate
		PMesh<WeilerAtherton.WeilerPoint> mesh = new PMesh<>((x,y) -> new WeilerAtherton.WeilerPoint(new VPoint(x,y), false, false));

		Set<VPoint> expectedPoints1 = new HashSet<>();
		Set<VPoint> expectedPoints2 = new HashSet<>();

		expectedPoints1.addAll(poly1.getPath());
		expectedPoints2.addAll(poly2.getPath());

		assertEquals(expectedPoints1, mesh.streamPoints(face1).map(p -> new VPoint(p)).collect(Collectors.toSet()));
		assertEquals(expectedPoints2, mesh.streamPoints(face2).map(p -> new VPoint(p)).collect(Collectors.toSet()));

	}

	@Test
	public void testIntersectionFaceConstructionIntersections() {
		VPolygon poly1 = GeometryUtils.toPolygon(new VPoint(0, 0), new VPoint(1, 1), new VPoint(1, -1));
		VPolygon poly2 = GeometryUtils.toPolygon(new VPoint(0.3, 0), new VPoint(-1, 1), new VPoint(-1, -1));

		WeilerAtherton weilerAtherton = new WeilerAtherton(poly1, poly2);
		Pair<PFace<WeilerAtherton.WeilerPoint>, PFace<WeilerAtherton.WeilerPoint>> pair = weilerAtherton.constructIntersectionFaces();

		PFace<WeilerAtherton.WeilerPoint> face1 = pair.getFirst();
		PFace<WeilerAtherton.WeilerPoint> face2 = pair.getSecond();

		// we need a mesh to iterate
		PMesh<WeilerAtherton.WeilerPoint> mesh = new PMesh<>((x,y) -> new WeilerAtherton.WeilerPoint(new VPoint(x,y), false, false));

		assertEquals(5, mesh.streamPoints(face1).map(p -> new VPoint(p)).collect(Collectors.toSet()).size());
		assertEquals(5, mesh.streamPoints(face2).map(p -> new VPoint(p)).collect(Collectors.toSet()).size());

	}

}