package com.kubazuch.geometry;

import com.kubazuch.DrawUtils;

import java.awt.*;
import java.awt.geom.Point2D;

public class Edge extends Segment implements Drawable {

	public enum Constraint {
		NONE(""), VERTICAL("Vertical"), HORIZONTAL("Horizontal");

		private final String humanReadable;

		Constraint(String humanReadable) {
			this.humanReadable = humanReadable;
		}

		@Override
		public String toString() {
			return humanReadable;
		}
	}

	Polygon parent;

	Vertex from, to;

	private boolean selected, highlighted;

	private Constraint constraint = Constraint.NONE;

	Edge next, prev;

	public Edge(Polygon parent, Vertex from, Vertex to) {
		super(from.getPoint2D(), to.getPoint2D());
		this.from = from;
		this.to = to;

		this.parent = parent;

		this.from.outEdge = this;
		this.to.inEdge = this;
	}

	public Vertex getDrawableFrom() {
		return from;
	}

	public Vertex getDrawableTo() {
		return to;
	}

	public void setFrom(Vertex from) {
		this.from = from;
		super.from = from.getPoint2D();
	}

	public void setTo(Vertex to) {
		this.to = to;
		super.to = to.getPoint2D();
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public void setConstraint(Constraint constraint) {
		if (constraint != Constraint.NONE && (prev.constraint == constraint || next.constraint == constraint))
			throw new IllegalArgumentException();

		this.constraint = constraint;
		switch (constraint) {
			case VERTICAL -> {
				double x = to.getX() + from.getX();
				from.setX((int) (x / 2));
				to.setX((int) (x / 2));
				parent.updateClockwiseness();
			}
			case HORIZONTAL -> {
				double y = to.getY() + from.getY();
				from.setY((int) (y / 2));
				to.setY((int) (y / 2));
				parent.updateClockwiseness();
			}
		}
	}

	public Drawable divide() {
		setConstraint(Constraint.NONE);

		Vertex middle = new Vertex(midpoint());
		Vertex oldTo = to;

		Edge newEdge = new Edge(parent, middle, oldTo);

		setTo(middle);

		middle.inEdge = this;

		this.next.prev = newEdge;
		newEdge.next = this.next;
		newEdge.prev = this;
		this.next = newEdge;

		parent.size++;

		return middle;
	}

	@Override
	public void draw(Graphics2D g) {
		Color old = g.getColor();

		if (selected)
			g.setColor(old.darker());
		else if (highlighted)
			g.setColor(old.brighter());

		if (selected || highlighted)
			DrawUtils.drawLine(g, this, DrawUtils.SELECTED_LINE_THICKNESS);
		else
			DrawUtils.drawLine(g, this);

		if (constraint != Constraint.NONE)
			DrawUtils.drawConstraint(g, constraint, midpoint());

		g.setColor(old);
	}

	@Override
	public void move(int dx, int dy) {
		from.translate(dx, dy);
		to.translate(dx, dy);

		if (prev.getConstraint() == Edge.Constraint.HORIZONTAL) {
			prev.getDrawableFrom().translate(0, dy);
		} else if (prev.getConstraint() == Edge.Constraint.VERTICAL) {
			prev.getDrawableFrom().translate(dx, 0);
		}

		if (next.getConstraint() == Edge.Constraint.HORIZONTAL) {
			next.getDrawableTo().translate(0, dy);
		} else if (next.getConstraint() == Edge.Constraint.VERTICAL) {
			next.getDrawableTo().translate(dx, 0);
		}

		parent.updateClockwiseness();
	}

	@Override
	public Drawable hitTest(Point point) {
		Drawable endpoint = from.hitTest(point);
		if (endpoint != null) return endpoint;
		endpoint = to.hitTest(point);
		if (endpoint != null) return endpoint;

		double Sx = to.getX() - from.getX();
		double Sy = to.getY() - from.getY();
		double Px = point.getX() - from.getX();
		double Py = point.getY() - from.getY();

		double len = Sx * Sx + Sy * Sy;
		double dot = Sx * Px + Sy * Py;

		if (len == 0) return null;

		double factor = dot / len;
		if (factor < 0 || factor > 1) return null;

		Point2D projection = new Point2D.Double(from.getX() + factor * Sx, from.getY() + factor * Sy);
		double dist = projection.distance(point);

		return dist < GeometryUtils.LINE_DETECTION_RANGE ? this : null;
	}

	public Edge getNext() {
		return next;
	}

	public Edge getPrev() {
		return prev;
	}

	@Override
	public String toString() {
		StringBuilder ret = new StringBuilder();
		ret.append("Edge{");

		if (selected)
			ret.append("selected, ");
		else if (highlighted)
			ret.append("highlighted, ");

		if (constraint != Constraint.NONE)
			ret.append("constraint=").append(constraint).append(", ");

		ret.append("from=").append(from).append(", to=").append(to).append('}');

		return ret.toString();
	}
}
