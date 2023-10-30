package com.kubazuch.geometry;

import java.awt.*;

public interface Drawable {
	void draw(Graphics2D g);

	void setSelected(boolean selected);

	void setHighlighted(boolean highlighted);

	void move(int dx, int dy);

	Drawable hitTest(Point hit);
}
