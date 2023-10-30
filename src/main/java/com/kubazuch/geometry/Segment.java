package com.kubazuch.geometry;

import java.awt.geom.Point2D;

public class Segment {
	protected Point2D from, to;

	public Segment(Point2D from, Point2D to) {
		this.from = from;
		this.to = to;
	}

	public Point2D getFrom() {
		return from;
	}

	public Point2D getTo() {
		return to;
	}

	public void setFrom(Point2D from) {
		this.from = from;
	}

	public void setTo(Point2D to) {
		this.to = to;
	}

	public boolean intersects(Segment other) {
		double d1 = GeometryUtils.crossProduct(from, to, other.from);
		double d2 = GeometryUtils.crossProduct(from, to, other.to);
		double d3 = GeometryUtils.crossProduct(other.from, other.to, from);
		double d4 = GeometryUtils.crossProduct(other.from, other.to, to);

		return (d1 * d2 < 0) && (d3 * d4 < 0);
	}

	public Point2D intersect(Segment other) {
//		if(!intersects(other)) return null;

		double determinant = (from.getX() - to.getX()) * (other.from.getY() - other.to.getY()) - (from.getY() - to.getY()) * (other.from.getX() - other.to.getX());

		double x = ((from.getX() * to.getY() - from.getY() * to.getX()) * (other.from.getX() - other.to.getX()) - (from.getX() - to.getX()) * (other.from.getX() * other.to.getY() - other.from.getY() * other.to.getX())) / determinant;
		double y = ((from.getX() * to.getY() - from.getY() * to.getX()) * (other.from.getY() - other.to.getY()) - (from.getY() - to.getY()) * (other.from.getX() * other.to.getY() - other.from.getY() * other.to.getX())) / determinant;

		return new Point2D.Double(x, y);
	}

	public Point2D midpoint() {
		double x = from.getX() + to.getX();
		double y = from.getY() + to.getY();
		return new Point2D.Double(x / 2, y / 2);
	}

	@Override
	public String toString() {
		return "Segment{" +
				"from=" + from +
				", to=" + to +
				'}';
	}
}
