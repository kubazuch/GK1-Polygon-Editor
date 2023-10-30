package com.kubazuch.component;

import javax.swing.*;
import java.awt.*;

public class ColorIcon implements Icon {
	private final Color color;

	public ColorIcon(Color color) {
		this.color = color;
	}

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(color);
		g.fillRect(x + 5, y, getIconWidth() - 5, getIconHeight());
	}

	@Override
	public int getIconWidth() {
		return 15;
	}

	@Override
	public int getIconHeight() {
		return 10;
	}
}
