package com.kubazuch.geometry;

import java.awt.geom.Point2D;
import java.util.*;

public class GeometryUtils {
    public static final int POINT_RADIUS = 6;
    public static final int LINE_DETECTION_RANGE = 6;

    public static boolean pointHitTest(Point2D point, Point2D hit) {
        return point.distance(hit) <= POINT_RADIUS;
    }

    public static <P extends Point2D> List<Segment> buildLineList(List<P> pointList, P potentialPoint) {
        ArrayList<Segment> edgeList = new ArrayList<>(pointList.size());

        Iterator<P> iter = pointList.iterator();
		P prev = iter.next();
        P first = prev;

        while(iter.hasNext()) {
			P curr = iter.next();

            edgeList.add(new Segment(prev, curr));

            prev = curr;
		}

        edgeList.add(new Segment(prev, potentialPoint == null ? first : potentialPoint));
        return edgeList;
    }

    public static double crossProduct(Point2D p1, Point2D p2, Point2D p3) {
        return (p2.getX() - p1.getX()) * (p3.getY() - p1.getY()) - (p2.getY() - p1.getY()) * (p3.getX() - p1.getX());
    }

    public static Point2D normalize(double x, double y) {
        double len = Math.sqrt(x*x+y*y);
        return new Point2D.Double(x/len, y/len);
    }

    public static Point2D vectorNormal(Point2D from, Point2D to) {
        Point2D tangent = normalize(to.getX() - from.getX(), to.getY() - from.getY());
        return new Point2D.Double(tangent.getY(), -tangent.getX());
    }
}
