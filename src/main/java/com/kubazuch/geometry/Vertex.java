package com.kubazuch.geometry;

import com.kubazuch.DrawUtils;

import java.awt.*;
import java.awt.geom.Point2D;

public class Vertex implements Drawable {
	private Point2D point2D;
	private boolean selected, highlighted;

	Edge inEdge, outEdge;

	public Vertex(Point2D point2D) {
		this.point2D = point2D;
	}

	public Point2D getPoint2D() {
		return point2D;
	}

	public double getX() {
		return point2D.getX();
	}

	public double getY() {
		return point2D.getY();
	}

	void setX(double x) {
		point2D.setLocation(x, point2D.getY());
	}

	void setY(double y) {
		point2D.setLocation(point2D.getX(), y);
	}

	public void setPoint2D(Point2D point2D) {
		this.point2D = point2D;
	}

	@Override
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		this.highlighted = highlighted;
	}

	@Override
	public void draw(Graphics2D g) {
		Color old = g.getColor();

        if(selected)
            g.setColor(old.darker());
        else if(highlighted)
            g.setColor(old.brighter());

		if(selected || highlighted)
			DrawUtils.drawPoint(g, point2D, DrawUtils.SELECTED_POINT_RADIUS);
		else
			DrawUtils.drawPoint(g, point2D);

        g.setColor(old);
	}

	@Override
	public void move(int dx, int dy) {
		translate(dx, dy);

		if(inEdge.getConstraint() == Edge.Constraint.HORIZONTAL) {
			inEdge.getDrawableFrom().translate(0, dy);
		} else if(inEdge.getConstraint() == Edge.Constraint.VERTICAL) {
			inEdge.getDrawableFrom().translate(dx, 0);
		}

		if(outEdge.getConstraint() == Edge.Constraint.HORIZONTAL) {
			outEdge.getDrawableTo().translate(0, dy);
		} else if(outEdge.getConstraint() == Edge.Constraint.VERTICAL) {
			outEdge.getDrawableTo().translate(dx, 0);
		}

        inEdge.parent.updateClockwiseness();
	}

	void translate(int dx, int dy) {
		int oldX = (int) point2D.getX();
		int oldY = (int) point2D.getY();
		point2D.setLocation(oldX + dx, oldY + dy);
	}

	@Override
	public Drawable hitTest(Point hit) {
		return GeometryUtils.pointHitTest(point2D, hit) ? this : null;
	}

	public Drawable delete() {
		if(inEdge.parent.size == 3) {
			inEdge.parent.delete();
			return null;
		}

		inEdge.setConstraint(Edge.Constraint.NONE);
		outEdge.setConstraint(Edge.Constraint.NONE);

		inEdge.next = outEdge.next;
		outEdge.next.prev = inEdge;

		inEdge.setTo(outEdge.next.from);
		outEdge.to.inEdge = inEdge;

		inEdge.parent.size--;

		inEdge.parent.firstEdge = inEdge;

		inEdge.parent.updateClockwiseness();

		return inEdge;
	}
}
