package com.kubazuch.geometry;

import com.kubazuch.DrawUtils;

import java.awt.geom.Point2D;
import java.util.*;
import java.awt.Color;
import java.awt.Point;
import java.awt.Graphics2D;
import java.util.function.Consumer;

public class Polygon implements Drawable {
	private static int ID = 0;

	Edge firstEdge;
	int size;

	private Color color;
	private boolean clockwise;
	private final int id;
	private final Consumer<Polygon> deletionHandler;

	public Polygon(LinkedList<Point> points, Color c, Consumer<Polygon> deletionHandler) {
		this.color = c;
		this.id = ID++;
		this.deletionHandler = deletionHandler;

		constructPointsAndEdges(points);
		updateClockwiseness();
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	@Override
	public void setSelected(boolean selected) {
		Queue<Vertex> points = new LinkedList<>();
		for (Edge segment : getEdges()) {
			segment.setSelected(selected);
			points.add(segment.getDrawableFrom());
		}

		for (Drawable point : points) {
			point.setSelected(selected);
		}
	}

	@Override
	public void setHighlighted(boolean highlighted) {
		Queue<Vertex> points = new LinkedList<>();
		for (Edge segment : getEdges()) {
			segment.setHighlighted(highlighted);
			points.add(segment.getDrawableFrom());
		}

		for (Drawable point : points) {
			point.setHighlighted(highlighted);
		}
	}

	@Override
	public void draw(Graphics2D g) {
		g.setColor(color);

		Queue<Vertex> points = new LinkedList<>();
		for (Edge segment : getEdges()) {
			segment.draw(g);
			points.add(segment.getDrawableFrom());
		}

		for (Drawable point : points) {
			point.draw(g);
		}
	}

	public void drawOffset(Graphics2D g, int offset) {
		class PointIndexed extends Point2D.Double {
			private final int id;

			private PointIndexed other;

			public PointIndexed(Point2D from, int id) {
				super(from.getX(), from.getY());
				this.id = id;
			}

			public PointIndexed(Point2D from, int id, PointIndexed other) {
				super(from.getX(), from.getY());
				this.id = id;
				this.other = other;
			}

			public PointIndexed getOther() {
				return other;
			}

			public void setOther(PointIndexed other) {
				this.other = other;
			}

			@Override
			public boolean equals(Object o) {
				if (this == o) return true;
				if (o == null || getClass() != o.getClass()) return false;
				if (!super.equals(o)) return false;

				PointIndexed that = (PointIndexed) o;

				return id == that.id;
			}

			@Override
			public int hashCode() {
				int result = super.hashCode();
				result = 31 * result + id;
				return result;
			}
		}

		class VertexEvent {
			final Point2D vertex;
			final int orientation;
			final int totalWinding;

			public VertexEvent(Point2D vertex, int orientation, int totalWinding) {
				this.vertex = vertex;
				this.orientation = orientation;
				this.totalWinding = totalWinding;
			}
		}

		g.setColor(color);
		if (offset == 0.0) // there is no need for drawing anything
			return;

		// Find naive offset vertices and corresponding edges
		List<Point2D> naive = getNaiveOffset(offset);
		List<Segment> segmentList = GeometryUtils.buildLineList(naive, null);

		// Encode lines in map and find leftmost vertex
		HashMap<Point2D, Point2D> segmentMap = new HashMap<>();
		Point2D starting = naive.get(0);
		Point2D prevv = naive.get(naive.size() - 1);
		for (Segment l : segmentList) {
			segmentMap.put(l.getFrom(), l.getTo());

			if (l.getFrom().getX() == starting.getX() ? l.getFrom().getY() < starting.getY() : l.getFrom().getX() < starting.getX())
				if((GeometryUtils.crossProduct(prevv, l.getFrom(), l.getTo()) > 0) == clockwise)
					starting = l.getFrom();

			prevv = l.getFrom();
		}

		// Find all intersections, split edges and interleave them
		for (int i = 0; i < segmentList.size(); i++) {
			Segment e1 = segmentList.get(i);
			for (int j = i + 1; j < segmentList.size(); j++) {
				Segment e2 = segmentList.get(j);
				if (e1.getFrom().equals(e2.getFrom()) || e1.getFrom().equals(e2.getTo()) || e1.getTo().equals(e2.getFrom()))
					continue;

				if (e1.intersects(e2)) {
					// Find intersection and encode it as two entangled points
					Point2D intersect = e1.intersect(e2);
					PointIndexed p1 = new PointIndexed(intersect, 0);
					PointIndexed p2 = new PointIndexed(intersect, 1, p1);
					p1.setOther(p2);

					// Update edges in map
					segmentMap.put(e1.getFrom(), p1);
					segmentMap.put(p1, e2.getTo());
					segmentMap.put(e2.getFrom(), p2);
					segmentMap.put(p2, e1.getTo());

					// Add new edges to list as new intersection candidates
					segmentList.add(new Segment(p1, e2.getTo()));
					segmentList.add(new Segment(p2, e1.getTo()));

					// Update edges
					e1.setTo(p1);
					e2.setTo(p2);
				}
			}
		}

		// Decompose new points into simple polygons by travelling through vertices and enqueuing not yet seen entangled vertices
		Queue<VertexEvent> queue = new LinkedList<>();
		TreeSet<Point2D> seen = new TreeSet<>(Comparator.comparingDouble(Point2D::getX).thenComparingDouble(Point2D::getY));
		queue.add(new VertexEvent(starting, 1, 0));

		while (!queue.isEmpty()) {
			VertexEvent start = queue.remove();

			LinkedList<Point2D> polygon = new LinkedList<>();
			polygon.add(start.vertex);

			// We will retrieve edges from previously built map
			Point2D prev = start.vertex;
			Point2D curr = segmentMap.get(prev);
			Point2D next = segmentMap.get(curr);
			while (!curr.equals(start.vertex)) {
				// Failsafe; this should never happen, but if it did, we would be stuck in this loop
				if (polygon.contains(curr))
					break;

				polygon.add(curr);
				if (curr instanceof PointIndexed && !seen.contains(curr)) {
					// We found new polygon candidate

					// We will define a vertex of a simple polygon as being either convex (right-turning
					// for a clockwise oriented polygon, or left-turning for a counter-clockwise polygon), or
					// concave (left-turning for a clockwise polygon, or right-turning for a counter-clockwise
					// polygon).

					// If curr is convex, then new polygon has opposite orientation as current
					// If curr is concave, then new polygon has the same orientation as current
					int orientation = (GeometryUtils.crossProduct(prev, curr, next) < 0 != start.orientation > 0) ? -start.orientation : start.orientation;

					// Add new polygon candidate to queue
					queue.add(new VertexEvent(((PointIndexed) curr).getOther(), orientation, start.totalWinding + orientation));
					seen.add(curr);
				}

				prev = curr;
				curr = segmentMap.get(prev);
				next = segmentMap.get(curr);
			}

			// Only "real" polygons are those with winding number = 0
			if (start.totalWinding != 0)
				continue;

			// Draw the polygon
			DrawUtils.drawOffset(g, polygon);
		}
	}

	@Override
	public void move(int dx, int dy) {
		Queue<Vertex> points = new LinkedList<>();
		for (Edge segment : getEdges()) {
			points.add(segment.getDrawableFrom());
		}

		for (Vertex point : points) {
			point.translate(dx, dy);
		}
	}

	@Override
	public Drawable hitTest(Point point) {
		for (Edge segment : getEdges()) {
			Drawable ret = segment.hitTest(point);
			if (ret != null)
				return ret;
		}

		boolean inside = false;
		for (Edge edge : getEdges()) {
			if ((edge.getFrom().getY() > point.y) != (edge.getTo().getY() > point.y) && point.x < (edge.getTo().getX() - edge.getFrom().getX()) * (point.y - edge.getFrom().getY()) / (edge.getTo().getY() - edge.getFrom().getY()) + edge.getFrom().getX())
				inside = !inside;
		}

		return inside ? this : null;
	}

	private void constructPointsAndEdges(LinkedList<Point> points) {
		ListIterator<Point> iter = points.listIterator();
		Vertex prev = new Vertex(iter.next());
		Vertex first = prev;

		while (iter.hasNext()) {
			Vertex curr = new Vertex(iter.next());

			add(new Edge(this, prev, curr));

			prev = curr;
		}

		add(new Edge(this, prev, first));
	}

	public void updateClockwiseness() {
		double determinant = 0;

		for (Edge edge : getEdges()) {
			Vertex prev = edge.from;
			Vertex curr = edge.to;

			determinant += (curr.getX() - prev.getX()) * (curr.getY() + prev.getY());
		}

		clockwise = determinant < 0;
	}

	private List<Point2D> getNaiveOffset(int offset) {
		List<Point2D> ret = new ArrayList<>();

		for (Edge edge : getEdges()) {
			Vertex curr = edge.from;
			Vertex prev = curr.inEdge.from;
			Vertex next = curr.outEdge.to;

			Point2D cnN = GeometryUtils.vectorNormal(curr.getPoint2D(), next.getPoint2D());
			Point2D pcN = GeometryUtils.vectorNormal(prev.getPoint2D(), curr.getPoint2D());
			Point2D bis = GeometryUtils.normalize(cnN.getX() + pcN.getX(), cnN.getY() + pcN.getY());

			double bisLen = offset / Math.sqrt((1 + cnN.getX() * pcN.getX() + cnN.getY() * pcN.getY()) / 2.0);
			if (!clockwise)
				bisLen = -bisLen;
			ret.add(new Point2D.Double(curr.getX() + bisLen * bis.getX(), curr.getY() + bisLen * bis.getY()));
		}

		return ret;
	}

	/*
	 * LinkedList utils
	 */

	void add(Edge e) {
		size++;

		if (firstEdge == null) {
			e.next = e.prev = e;
			firstEdge = e;
			return;
		}

		final Edge l = firstEdge.prev;

		e.next = firstEdge;
		firstEdge.prev = e;
		e.prev = l;
		l.next = e;
	}

	public Iterable<Edge> getEdges() {
		return EdgeIterator::new;
	}

	public int getSize() {
		return size;
	}

	void delete() {
		deletionHandler.accept(this);
	}

	@Override
	public String toString() {
		return "Polygon " + id + " (" + size + " edges)";
	}

	public class EdgeIterator implements Iterator<Edge> {
		private Edge next;
		private int nextIndex;

		public EdgeIterator() {
			next = firstEdge;
			nextIndex = 0;
		}

		@Override
		public boolean hasNext() {
			return nextIndex < size;
		}

		@Override
		public Edge next() {
			if (!hasNext())
				throw new NoSuchElementException();

			Edge ret = next;
			next = next.next;
			nextIndex++;

			return ret;
		}
	}
}
