package org.vadere.util.triangulation.triangulator;

import org.vadere.util.geometry.mesh.inter.*;
import org.vadere.util.geometry.shapes.IPoint;
import org.vadere.util.geometry.shapes.VLine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by bzoennchen on 16.01.18.
 */
public class RuppertsTrinangulator<P extends IPoint, V extends IVertex<P>, E extends IHalfEdge<P>, F extends IFace<P>> implements ITriangulator{

    private final ITriangulation<P, V, E, F> triangulation;
    private final Collection<VLine> constrains;
    private final Set<P> points;

    /**
     *
     *
     * @param triangulation the triangulation which will be manipulated
     * @param constrains
     * @param points        a set of additional points, this set might be empty
     */
    public RuppertsTrinangulator(final ITriangulation<P, V, E, F> triangulation, final Collection<VLine> constrains, final Set<P> points) {
        this.triangulation = triangulation;
        this.constrains = constrains;
        this.points = points;
    }

    // TODO: not finished jet
    @Override
    public void generate() {
        triangulation.init();
        Collection<P> allPoints = new ArrayList<>();
        IMesh<P, V, E, F> mesh = triangulation.getMesh();

        triangulation.insert(points);
        Collection<P> constraintPoints = constrains.stream()
                .flatMap(line -> Stream.of(mesh.createPoint(line.getX1(), line.getY1()), mesh.createPoint(line.getX2(), line.getY2())))
                .collect(Collectors.toList());

        for(P contraintPoint : constraintPoints) {
            E edge = triangulation.insert(contraintPoint);
            V vertex = mesh.getVertex(edge);

        }

        // recover edges
        for(VLine line : constrains) {
            // get all neighbours of p1
            // if any is inside the diameter
            // cut line into 2 and repeat recursively (if the point lies on the line do not introduce new lines)
        }

        throw new UnsupportedOperationException("implementation is not finished jet.");
    }
}