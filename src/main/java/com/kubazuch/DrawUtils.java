package com.kubazuch;

import com.kubazuch.geometry.Edge;
import com.kubazuch.geometry.GeometryUtils;
import com.kubazuch.geometry.Segment;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.LinkedList;

public class DrawUtils {
    public enum LineAlgorithm {
        BUILTIN, BRESENHAM
    }

    public static final int SELECTED_POINT_RADIUS = GeometryUtils.POINT_RADIUS * 5 / 4;
    public static final int SELECTED_LINE_THICKNESS = 3;
    public static LineAlgorithm lineAlgorithm = LineAlgorithm.BUILTIN;

    public static void drawPoint(Graphics2D g, Point2D p) {
        drawPoint(g, p, GeometryUtils.POINT_RADIUS);
    }

    public static void drawPoint(Graphics2D g, Point2D p, int radius) {
        g.fillOval((int) p.getX() - radius, (int) p.getY() - radius, 2 * radius, 2 * radius);
    }

    public static void drawLine(Graphics2D g, Segment segment, int thickness) {
        Stroke old = g.getStroke();

        g.setStroke(new BasicStroke(thickness));
        drawLine(g, segment);
        g.setStroke(old);
    }

    public static void drawLine(Graphics2D g, Segment segment) {
        switch (lineAlgorithm) {
            case BUILTIN:
                g.drawLine((int) segment.getFrom().getX(), (int) segment.getFrom().getY(), (int) segment.getTo().getX(), (int) segment.getTo().getY());
                break;
            case BRESENHAM:
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                midpointLine(g, (int) segment.getFrom().getX(), (int) segment.getFrom().getY(), (int) segment.getTo().getX(), (int) segment.getTo().getY());
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                break;
        }
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private static void midpointLine(Graphics2D g, int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        int incrX = (dx >= 0) ? 1 : -1;
        int incrY = (dy >= 0) ? 1 : -1;
        dx = (dx < 0) ? -dx : dx;
        dy = (dy < 0) ? -dy : dy;

        if (dx >= dy) {
            midpointLineInt(x1, y1, dx, dy, incrX, incrY, (x, y) -> g.drawLine(x, y, x, y));
        } else {
            midpointLineInt(y1, x1, dy, dx, incrY, incrX, (y, x) -> g.drawLine(x, y, x, y));
        }
    }
    private static void putCirclePixel(Graphics2D g, Point center, int x, int y, float I) {
        g.setColor(new Color(0, 0, 0, I));
        int centerX = center.x;
        int centerY = center.y;
        g.drawLine(centerX + x, centerY + y, centerX + x, centerY + y);
        g.drawLine(centerX - x, centerY + y, centerX - x, centerY + y);
        g.drawLine(centerX - x, centerY - y, centerX - x, centerY - y);
        g.drawLine(centerX + x, centerY - y, centerX + x, centerY - y);
    }

    public static void drawWuCirlce(Graphics2D g, Point center, int radius) {
        double quarter = Math.round(radius / Math.sqrt(2));
        double T = 0.0;
        for (int _x = 0, _y = radius; _x <= quarter; _x++) {
            double d = Math.sqrt(radius * radius - _x * _x);
            double D = Math.ceil(d) - d;
            if (D < T) {
                _y--;
            }

            putCirclePixel(g, center, _x, _y, 1.f - (float) D);
            putCirclePixel(g, center, _x, _y - 1, (float) D);
            T = D;
        }

        for (int _y = 0, _x = radius + 1; _y <= quarter; _y++) {
            double d = Math.sqrt(radius * radius - _y * _y);
            double D = Math.ceil(d) - d;
            if (D < T) {
                _x--;
            }

            putCirclePixel(g, center, _x, _y, 1.f - (float) D);
            putCirclePixel(g, center, _x - 1, _y, (float) D);
            T = D;
        }
    }

    private static void midpointLineInt(int x1, int y1, int dx, int dy, int incr_x, int incr_y, PointPutter putter) {
        int d = 2 * dy - dx;

        int incrE = 2 * dy;
        int incrNE = 2 * (dy - dx);

        int x = 0;
        int y = 0;
        putter.putPoint(x, y);
        for (int i = 0; i <= dx; i++) {
            putter.putPoint(x1 + x, y1 + y);
            if (d < 0) {
                d += incrE;
                x += incr_x;
            } else {
                d += incrNE;
                x += incr_x;
                y += incr_y;
            }
        }
    }

    public static <T extends Point2D> void drawPolygonalChain(Graphics2D g, LinkedList<T> chain, T potentialPoint) {
        if (chain.isEmpty()) return;

        for (Segment segment : GeometryUtils.buildLineList(chain, potentialPoint)) {
            drawPoint(g, segment.getFrom());
            drawLine(g, segment);
        }
    }

    public static void drawOffset(Graphics2D g, LinkedList<Point2D> polygon) {
        Stroke old = g.getStroke();

        if (lineAlgorithm == LineAlgorithm.BUILTIN)
            g.setStroke(new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));

        for (Segment segment : GeometryUtils.buildLineList(polygon, null)) {
            drawLine(g, segment);
        }
        g.setStroke(old);
    }

    private static Shape verticalShape;
    private static Shape horizontalShape;

    public static void drawConstraint(Graphics2D g, Edge.Constraint constraint, Point2D point) {
        Color oldColor = g.getColor();
        AffineTransform oldTransform = g.getTransform();
        Stroke oldStroke = g.getStroke();
        FontRenderContext frc = g.getFontRenderContext();

        if (constraint == Edge.Constraint.VERTICAL && verticalShape == null) {
            TextLayout tl = new TextLayout("V", g.getFont().deriveFont(24f), frc);
            verticalShape = tl.getOutline(null);
        } else if (constraint == Edge.Constraint.HORIZONTAL && horizontalShape == null) {
            TextLayout tl = new TextLayout("H", g.getFont().deriveFont(24f), frc);
            horizontalShape = tl.getOutline(null);
        }

        Shape shape = constraint == Edge.Constraint.VERTICAL ? verticalShape : horizontalShape;
        Rectangle bounds = shape.getBounds();
        int x = (int) (point.getX() - bounds.width / 2);
        int y = (int) (point.getY() + bounds.height / 2);

        AffineTransform transform = (AffineTransform) oldTransform.clone();
        transform.translate(x, y);
        g.transform(transform);

        g.setStroke(new BasicStroke(3f));
        g.setColor(Color.BLACK);
        g.draw(shape);
        g.setColor(Color.WHITE);
        g.fill(shape);

        g.setColor(oldColor);
        g.setTransform(oldTransform);
        g.setStroke(oldStroke);
    }

    private interface PointPutter {
        void putPoint(int x, int y);
    }
}
