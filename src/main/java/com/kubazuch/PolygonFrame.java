package com.kubazuch;

import com.kubazuch.component.Canvas;
import com.kubazuch.component.ColorChooserButton;
import com.kubazuch.component.ListItemTransferHandler;
import com.kubazuch.component.PolygonRenderer;
import com.kubazuch.geometry.Edge;
import com.kubazuch.geometry.Polygon;

import javax.swing.*;
import java.awt.*;

public class PolygonFrame extends JFrame {

	public static final PolygonFrame INSTANCE = new PolygonFrame();

	private JPanel mainPanel;
	private JPanel canvas;
	private JPanel sidePanel;
	public JList<Polygon> polygonList;
	public JPanel infoPanel;
	private JPanel toolsPanel;
	private JScrollPane scrollPane;
	private JPanel polygonPanel;
	private JPanel edgePanel;
	private JPanel vertexPanel;
	public ColorChooserButton polygonColor;
	private JPanel emptyPanel;
	public JLabel edgeCount;
	public JButton polygonDelete;
	public JComboBox<Edge.Constraint> constraintBox;
	public JButton edgeSplit;
	public JButton vertexDelete;
	public JRadioButton builtInRadioButton;
	public JRadioButton bresenhamRadioButton;
	public JSlider offsetSlider;
	public JButton clearButton;

	/******************************************************/

	private PolygonFrame() {
		setTitle("Polygon Drawer");
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setContentPane(mainPanel);

		SwingUtilities.invokeLater(this::showKeybindings);
	}

	private void createUIComponents() {
		infoPanel = new JPanel();

		polygonList = new JList<>();
		polygonList.setCellRenderer(new PolygonRenderer());
		polygonList.setDragEnabled(true);
		polygonList.setTransferHandler(new ListItemTransferHandler());

		polygonColor = new ColorChooserButton(Color.BLACK);

		constraintBox = new JComboBox<>(Edge.Constraint.values());
		edgeSplit = new JButton();
		polygonDelete = new JButton();
		vertexDelete = new JButton();

		builtInRadioButton = new JRadioButton();
		bresenhamRadioButton = new JRadioButton();
		offsetSlider = new JSlider();
		clearButton = new JButton();

		canvas = new Canvas(this);

		JMenuBar menuBar = new JMenuBar();
		JMenu helpMenu = new JMenu("Help");
		JMenuItem keyMenuItem = new JMenuItem("Controls");
		keyMenuItem.addActionListener(l -> {
			showKeybindings();
		});
		helpMenu.add(keyMenuItem);
		menuBar.add(helpMenu);

		setJMenuBar(menuBar);
	}

	private void showKeybindings() {
		String keybindings = """
				Mouse:
				    LMB - move, draw
				    MMB - divide edge
				    RMB - begin draw
				  
				Keyboard:
				    V - vertical constraint
				    H - horizontal constraint
				    C - clear constraint
				    Del - delete""";

		JOptionPane.showMessageDialog(this, keybindings, "Controls", JOptionPane.INFORMATION_MESSAGE);
	}
}
