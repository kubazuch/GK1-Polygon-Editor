package com.kubazuch.component;

import com.kubazuch.geometry.Polygon;

import javax.swing.*;
import java.awt.*;

public class PolygonRenderer extends JLabel implements ListCellRenderer<Polygon> {

	public PolygonRenderer() {
		setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList<? extends Polygon> list, Polygon value, int index, boolean isSelected, boolean cellHasFocus) {
		setIcon(new ColorIcon(value.getColor()));
		setText(value.toString());

		setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
		setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());

		return this;
	}
}
