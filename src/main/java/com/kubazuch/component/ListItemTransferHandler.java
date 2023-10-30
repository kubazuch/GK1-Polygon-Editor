package com.kubazuch.component;

import com.kubazuch.geometry.Polygon;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.Objects;

// https://stackoverflow.com/questions/16586562/reordering-jlist-with-drag-and-drop
public class ListItemTransferHandler extends TransferHandler {
	protected final DataFlavor localObjectFlavor;
	protected int index = -1;
	protected int addIndex = -1;

	public ListItemTransferHandler() {
		super();
		localObjectFlavor = new DataFlavor(Polygon.class, "Polygon");
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		@SuppressWarnings("unchecked") JList<Polygon> source = (JList<Polygon>) c;
		c.getRootPane().getGlassPane().setVisible(true);

		index = source.getSelectedIndex();
		Polygon transferredObject = source.getSelectedValue();
		return new Transferable() {
			@Override
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[]{localObjectFlavor};
			}

			@Override
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return Objects.equals(localObjectFlavor, flavor);
			}

			@Override
			public Object getTransferData(DataFlavor flavor)
					throws UnsupportedFlavorException {
				if (isDataFlavorSupported(flavor)) {
					return transferredObject;
				} else {
					throw new UnsupportedFlavorException(flavor);
				}
			}
		};
	}

	@Override
	public boolean canImport(TransferSupport info) {
		return info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
	}

	@Override
	public int getSourceActions(JComponent c) {
		Component glassPane = c.getRootPane().getGlassPane();
		glassPane.setCursor(DragSource.DefaultMoveDrop);
		return MOVE; // COPY_OR_MOVE;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean importData(TransferSupport info) {
		TransferHandler.DropLocation tdl = info.getDropLocation();
		if (!canImport(info) || !(tdl instanceof JList.DropLocation dl))
			return false;

		JList<Polygon> target = (JList<Polygon>) info.getComponent();
		BetterListModel<Polygon> listModel = (BetterListModel<Polygon>) target.getModel();

		int max = listModel.getSize();
		int index = dl.getIndex();
		index = index < 0 ? max : index; // If it is out of range, it is appended to the end
		index = Math.min(index, max);

		addIndex = index;

		try {
			Polygon value = (Polygon) info.getTransferable().getTransferData(localObjectFlavor);
			listModel.add(index, value);
			target.addSelectionInterval(index, index);
			return true;
		} catch (UnsupportedFlavorException | IOException ex) {
			ex.printStackTrace();
		}

		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		c.getRootPane().getGlassPane().setVisible(false);
		cleanup(c, action == MOVE);
	}

	@SuppressWarnings("unchecked")
	private void cleanup(JComponent c, boolean remove) {
		if (remove && index >= 0) {
			if(index >= addIndex)
				index++;

			JList<Polygon> source = (JList<Polygon>) c;
			BetterListModel<Polygon> model = (BetterListModel<Polygon>) source.getModel();
			model.remove(index);
		}

		index = -1;
		addIndex = -1;
	}
}
