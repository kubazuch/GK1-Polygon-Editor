package com.kubazuch.geometry;

import com.kubazuch.DrawUtils;

import java.awt.*;

public class Circle implements Drawable{
    private Point center;
    private int radius;

    public Circle(Point center, int radius) {
        this.center = center;
        this.radius = radius;
    }

    public Point getCenter() {
        return center;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    @Override
    public void draw(Graphics2D g) {
        DrawUtils.drawWuCirlce(g, center, radius);
    }

    @Override
    public void setSelected(boolean selected) {
    }

    @Override
    public void setHighlighted(boolean highlighted) {
    }

    @Override
    public void move(int dx, int dy) {
        center.translate(dx, dy);
    }

    @Override
    public Drawable hitTest(Point hit) {
        return center.distance(hit) <= radius ? this : null;
    }
}
