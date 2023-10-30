package com.kubazuch.component;

import com.kubazuch.DrawUtils;
import com.kubazuch.PolygonFrame;
import com.kubazuch.geometry.*;
import com.kubazuch.geometry.Polygon;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

public class Canvas extends JPanel implements MouseMotionListener {

    public enum State {
        IDLE, DRAW
    }

    private final Random random;

    private State canvasState;
    private Point mousePos = new Point(-1, -1);
    private Point lastDrag;
    private int offset = 0;

    private Point circleMiddle;
    private LinkedList<Point> drawing;
    private final BetterListModel<Polygon> polygons;
    private Drawable selection;
    private Drawable highlight;

    private final PolygonFrame parent;

    public Canvas(PolygonFrame parent) {
        super();
        this.random = new Random();

        this.canvasState = State.IDLE;
        this.lastDrag = new Point(0, 0);

        this.drawing = new LinkedList<>();
        this.polygons = new BetterListModel<>(new ArrayList<>());

        this.parent = parent;

        setupLayout();
        setupStartupStage();

    }

    private void setupStartupStage() {
        Polygon p1 = new Polygon(new LinkedList<>(Arrays.asList(new Point(90, 30), new Point(330, 30), new Point(325, 260), new Point(100, 260), new Point(50, 170), new Point(100, 170), new Point(110, 200), new Point(240, 200), new Point(242, 60), new Point(90, 65), new Point(85, 130), new Point(20, 130))), Color.GREEN, this::deletePolygon);
        Edge.Constraint last = Edge.Constraint.HORIZONTAL;
        for (Edge e : p1.getEdges()) {
            e.setConstraint(last);
            if (last == Edge.Constraint.VERTICAL) {
                last = Edge.Constraint.HORIZONTAL;
            } else {
                last = Edge.Constraint.VERTICAL;
            }
        }
        polygons.addFirst(p1);

        Polygon p2 = new Polygon(new LinkedList<>(Arrays.asList(new Point(128, 320), new Point(128, 463), new Point(163, 460), new Point(166, 393), new Point(219, 460), new Point(219, 416), new Point(171, 370), new Point(240, 316), new Point(186, 313), new Point(163, 349), new Point(158, 308))), Color.CYAN, this::deletePolygon);
        Iterator<Edge> iterator = p2.getEdges().iterator();
        iterator.next().setConstraint(Edge.Constraint.VERTICAL);
        iterator.next().setConstraint(Edge.Constraint.HORIZONTAL);
        iterator.next().setConstraint(Edge.Constraint.VERTICAL);
        iterator.next();
        iterator.next().setConstraint(Edge.Constraint.VERTICAL);
        iterator.next();
        iterator.next();
        iterator.next().setConstraint(Edge.Constraint.HORIZONTAL);
        iterator.next();
        iterator.next().setConstraint(Edge.Constraint.VERTICAL);
        iterator.next().setConstraint(Edge.Constraint.HORIZONTAL);

        polygons.addFirst(p2);
    }

    /*
     * Polygon manipulation
     */
    private void testMousePosHighlight() {
        boolean flag = false;

        for (Polygon polygon : polygons) {
            Drawable target = polygon.hitTest(mousePos);
            if (target != null) {
                flag = true;

                if (highlight != null)
                    highlight.setHighlighted(false);
                highlight = target;
                highlight.setHighlighted(true);

                break;
            }
        }

        if (!flag && highlight != null) {
            highlight.setHighlighted(false);
            highlight = null;
        }
    }

    private void setSelection(Drawable sel) {
        if (selection != null)
            selection.setSelected(false);

        if (sel != null) {
            selection = sel;
            selection.setSelected(true);
        } else {
            selection = null;
        }

        updateSelectionInfo();
        repaint();
    }

    private boolean setEdgeConstraint(Edge edge, Edge.Constraint constraint) {
        boolean threw = false;
        try {
            edge.setConstraint(constraint);
        } catch (IllegalArgumentException exc) {
            JOptionPane.showMessageDialog(this, "Constraint already assigned to neighboring edge", "Critical error", JOptionPane.ERROR_MESSAGE);
            threw = true;
        }

        repaint();
        return !threw;
    }

    private void deletePolygon(Polygon poly) {
        this.polygons.remove(poly);
        setSelection(null);
        repaint();
    }

    private void splitEdge(Edge edge) {
        setSelection(edge.divide());
        parent.polygonList.repaint();
        repaint();
    }

    private void deletePoint(Vertex point) {
        setSelection(point.delete());
        parent.polygonList.repaint();
        repaint();
    }

    /*
     *  Layout
     */
    private void setupLayout() {
        parent.polygonList.setModel(polygons);
        parent.polygonList.addListSelectionListener(e -> setSelection(parent.polygonList.getSelectedValue()));

        parent.polygonColor.addColorChangedListener(c -> {
            if (selection instanceof Polygon p)
                p.setColor(c);

            repaint();
        });

        parent.constraintBox.addItemListener(event -> {
            if (selection instanceof Edge edge && event.getStateChange() == ItemEvent.SELECTED) {
                if (!setEdgeConstraint(edge, (Edge.Constraint) event.getItem()))
                    parent.constraintBox.setSelectedItem(Edge.Constraint.NONE);
            }
        });

        parent.edgeSplit.addActionListener(e -> {
            if (selection instanceof Edge edge) {
                splitEdge(edge);
            }
        });

        parent.polygonDelete.addActionListener(e -> {
            if (selection instanceof Polygon polygon) {
                deletePolygon(polygon);
            }
        });

        parent.vertexDelete.addActionListener(e -> {
            if (selection instanceof Vertex vertex) {
                deletePoint(vertex);
            }
        });

        parent.builtInRadioButton.addActionListener(e -> {
            DrawUtils.lineAlgorithm = DrawUtils.LineAlgorithm.BUILTIN;
            repaint();
        });

        parent.bresenhamRadioButton.addActionListener(e -> {
            DrawUtils.lineAlgorithm = DrawUtils.LineAlgorithm.BRESENHAM;
            repaint();
        });

        parent.offsetSlider.addChangeListener(e -> {
            offset = parent.offsetSlider.getValue();
            repaint();
        });

        parent.clearButton.addActionListener(e -> {
            polygons.clear();
            repaint();
        });

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e))
                    onLMBPressed(e);
                else if (SwingUtilities.isMiddleMouseButton(e))
                    onMMBPressed();
                else if (SwingUtilities.isRightMouseButton(e))
                    onRMBPressed(e);
            }
        });

        addMouseMotionListener(this);
        setKeyBindings();
    }

    private void setKeyBindings() {
        ActionMap actionMap = getActionMap();
        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);

        class ConstraintAction extends AbstractAction {
            private final Edge.Constraint constraint;

            public ConstraintAction(Edge.Constraint constraint) {
                this.constraint = constraint;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                if (selection instanceof Edge edge) {
                    if (setEdgeConstraint(edge, constraint))
                        parent.constraintBox.setSelectedItem(constraint);
                }
            }
        }

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, 0), Edge.Constraint.VERTICAL);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), Edge.Constraint.HORIZONTAL);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, 0), Edge.Constraint.NONE);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DEL");

        actionMap.put(Edge.Constraint.VERTICAL, new ConstraintAction(Edge.Constraint.VERTICAL));
        actionMap.put(Edge.Constraint.HORIZONTAL, new ConstraintAction(Edge.Constraint.HORIZONTAL));
        actionMap.put(Edge.Constraint.NONE, new ConstraintAction(Edge.Constraint.NONE));
        actionMap.put("DEL", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selection instanceof Vertex point) {
                    deletePoint(point);
                }

                if (selection instanceof Polygon poly) {
                    deletePolygon(poly);
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D graphics2d = (Graphics2D) g;


        graphics2d.setColor(Color.WHITE);
        graphics2d.fillRect(0, 0, getWidth(), getHeight());

        graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);


        graphics2d.setColor(Color.BLACK);

        if (!drawing.isEmpty())
            DrawUtils.drawPolygonalChain(graphics2d, drawing, drawing.getFirst().distance(mousePos) <= GeometryUtils.POINT_RADIUS ? null : mousePos);

        for (ListIterator<Polygon> it = polygons.listIterator(polygons.getSize()); it.hasPrevious(); ) {
            Polygon polygon = it.previous();

            polygon.drawOffset(graphics2d, offset);
            polygon.draw(graphics2d);
        }
    }

    public void onLMBPressed(MouseEvent e) {
        lastDrag = e.getPoint();
        switch (canvasState) {
            case IDLE -> {
                setSelection(highlight);

                if (selection instanceof Polygon poly) {
                    parent.polygonList.setSelectedValue(poly, true);
                } else {
                    parent.polygonList.clearSelection();
                }
            }
            case DRAW -> {
                if (drawing.getFirst().distance(e.getPoint()) <= GeometryUtils.POINT_RADIUS) {
                    if (drawing.size() > 2) {
                        polygons.addFirst(new Polygon(drawing, Color.getHSBColor(random.nextFloat(), 1.0f, 0.75f), this::deletePolygon));
                        drawing = new LinkedList<>();
                        canvasState = State.IDLE;
                    }
                } else {
                    drawing.add(e.getPoint());
                }

                repaint();
            }
        }
    }

    public void onMMBPressed() {
        if (canvasState == State.IDLE) {
            if (selection != null && selection instanceof Edge edge) {
                splitEdge(edge);
            }
        }
    }

    public void onRMBPressed(MouseEvent e) {
        switch (canvasState) {
            case IDLE -> {
                canvasState = State.DRAW;
                drawing.add(e.getPoint());
                repaint();
            }
            case DRAW -> {
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePos = e.getPoint();

        if (canvasState == State.DRAW) {
            repaint();
            return;
        }

        if (selection == null) return;

        Point now = e.getPoint();
        selection.move(now.x - lastDrag.x, now.y - lastDrag.y);
        lastDrag = now;
        super.repaint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();

        if (canvasState == State.DRAW) {
            super.repaint();
            return;
        }

        repaint();
    }

    private void updateSelectionInfo() {
        CardLayout cardLayout = (CardLayout) parent.infoPanel.getLayout();
        if (selection instanceof Polygon polygon) {
            cardLayout.show(parent.infoPanel, "PolygonCard");
            ((TitledBorder) parent.infoPanel.getBorder()).setTitle("Polygon");
            parent.edgeCount.setText(String.valueOf(polygon.getSize()));
            parent.polygonColor.setSelectedColor(polygon.getColor(), false);
            parent.infoPanel.repaint();
        } else if (selection instanceof Edge edge) {
            ((TitledBorder) parent.infoPanel.getBorder()).setTitle("Edge");
            cardLayout.show(parent.infoPanel, "EdgeCard");
            parent.constraintBox.setSelectedItem(edge.getConstraint());
            parent.infoPanel.repaint();
        } else if (selection instanceof Vertex) {
            ((TitledBorder) parent.infoPanel.getBorder()).setTitle("Vertex");
            cardLayout.show(parent.infoPanel, "VertexCard");
            parent.infoPanel.repaint();
        } else {
            ((TitledBorder) parent.infoPanel.getBorder()).setTitle("Selection");
            cardLayout.show(parent.infoPanel, "NoneCard");
            parent.infoPanel.repaint();
        }
    }

    @Override
    public void repaint() {
        if (polygons != null)
            testMousePosHighlight();
        super.repaint();
    }
}