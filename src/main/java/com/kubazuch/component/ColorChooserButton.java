package com.kubazuch.component;

import javax.swing.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

// https://stackoverflow.com/questions/26565166/how-to-display-a-color-selector-when-clicking-a-button
public class ColorChooserButton extends JButton {
	private Color selectedColor;

	public ColorChooserButton(Color current) {
		setSelectedColor(current);
		addActionListener(e -> {
			Color color = JColorChooser.showDialog(null, "Choose a color", selectedColor);
			setSelectedColor(color);
		});
	}

	public Color getSelectedColor() {
		return selectedColor;
	}

	public void setSelectedColor(Color newColor) {
		setSelectedColor(newColor, true);
	}

	public void setSelectedColor(Color newColor, boolean notify) {
		if(newColor == null) return;

		selectedColor = newColor;
		setIcon(new ColorIcon(selectedColor));
		repaint();

		if(notify) {
			for(ColorChangedListener listener : listeners) {
				listener.colorChanged(selectedColor);
			}
		}
	}

	public interface ColorChangedListener {
		void colorChanged(Color newColor);
	}

	private List<ColorChangedListener> listeners = new ArrayList<>();

	public void addColorChangedListener(ColorChangedListener toAdd) {
		listeners.add(toAdd);
	}
}
