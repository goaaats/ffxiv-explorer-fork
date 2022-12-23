package com.fragmenterworks.ffxivextract.gui.components;

import com.fragmenterworks.ffxivextract.helpers.Utils;
import com.fragmenterworks.ffxivextract.models.sqpack.index.SqPackIndexFile;

import javax.swing.*;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class EXL_View extends JScrollPane {

	private final SqPackIndexFile indexFile;
	private JTable table;

	public EXL_View(SqPackIndexFile indexFile, String path) {
		this.indexFile = indexFile;
		table = new JTable();
		table.setAutoCreateRowSorter(true);

		try {
			var data = indexFile.extractFile(path);
			var dataStr = new String(data);
			table.setModel(new EXLTableModel(dataStr));
		} catch (Exception e) {
			Utils.getGlobalLogger().error("", e);
		}

		setViewportView(table);

		var renderer = new DefaultTableCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.LEFT);
		table.getColumnModel().getColumn(1).setCellRenderer(renderer);
	}
}

class EXLTableModel extends AbstractTableModel {

	final EXLEntry[] entries;

	EXLTableModel(String content) {
		var items = content.split("\r\n");
		entries = new EXLEntry[items.length - 1];

		try {
			int i = 0;
			boolean skipFirst = true;
			for (var item : items) {
				if (skipFirst) {
					skipFirst = false;
					continue;
				}

				var values = item.split(",");
				var name = values[0];
				var id = Integer.parseInt(values[1]);
				entries[i] = new EXLEntry(name, id);
				i++;
			}
		} catch (Exception e) {
			Utils.getGlobalLogger().error("", e);
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}

	@Override
	public int getRowCount() {
		return entries.length;
	}

	@Override
	public String getColumnName(int column) {
		if (column == 0)
			return "Sheet Name";
		else if (column == 1)
			return "Sheet Index";
		return "";
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		try {
			var rowValue = entries[rowIndex];
			var value = columnIndex == 0 ? rowValue.name : rowValue.id;
			return value;
		} catch (Exception e) {
			Utils.getGlobalLogger().error("", e);
		}
		return "";
	}

	@Override
	public Class<?> getColumnClass(int column) {
		if (column == 0)
			return String.class;
		if (column == 1)
			return Integer.class;
		return super.getColumnClass(column);
	}
}

class EXLEntry {
	public final String name;
	public final int id;

	public EXLEntry(String name, int id) {
		this.name = name;
		this.id = id;
	}
}