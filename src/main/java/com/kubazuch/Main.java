package com.kubazuch;

import com.formdev.flatlaf.FlatDarculaLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
	    FlatDarculaLaf.setup();

		JFrame frame = PolygonFrame.INSTANCE;
		frame.pack();
		frame.setMinimumSize(frame.getSize());
		frame.setVisible(true);
    }
}