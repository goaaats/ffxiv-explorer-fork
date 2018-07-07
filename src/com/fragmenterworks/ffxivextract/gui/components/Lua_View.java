package com.fragmenterworks.ffxivextract.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

@SuppressWarnings("serial")
public class Lua_View extends JScrollPane {

	JTable luaCodeTable;
	String[] codeLines;

	public Lua_View(String[] strings) {
		
		if (strings != null)
			codeLines = strings;
		
		luaCodeTable = new JTable(new LuaCodeTableModel());
		
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL
				| GridBagConstraints.VERTICAL;
		getViewport().add(luaCodeTable);
		luaCodeTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
	    TableColumn tableColumn = luaCodeTable.getColumnModel().getColumn(0);
	    int preferredWidth = tableColumn.getMinWidth();
	    int maxWidth = tableColumn.getMaxWidth();
	 
	    for (int row = 0; row < luaCodeTable.getRowCount(); row++)
	    {
	        TableCellRenderer cellRenderer = luaCodeTable.getCellRenderer(row, 0);
	        Component c = luaCodeTable.prepareRenderer(cellRenderer, row, 0);
	        int width = c.getPreferredSize().width + luaCodeTable.getIntercellSpacing().width;
	        preferredWidth = Math.max(preferredWidth, width);
	 
	        //  We've exceeded the maximum width, no need to check other rows
	 
	        if (preferredWidth >= maxWidth)
	        {
	            preferredWidth = maxWidth;
	            break;
	        }
	    }
	 
	    tableColumn.setMinWidth( preferredWidth );
	    tableColumn.setMaxWidth( preferredWidth );
	   		
		//Graphics Stuff		
		DefaultTableCellRenderer cellRender = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setFont(header.getFont());
				}				
				
				if (column == 0)
					setHorizontalAlignment(JLabel.RIGHT);
				else
					setHorizontalAlignment(JLabel.LEFT);
				
				setText((value == null) ? "" : value.toString());
								

				if (column == 0){
					setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2,
							Color.LIGHT_GRAY));
					setForeground(Color.LIGHT_GRAY);
				}
				else
				{
					setForeground(Color.BLACK);
					setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
				}

				return this;
			}
		};
		luaCodeTable.setTableHeader(null);
		luaCodeTable.setShowGrid(false);
		luaCodeTable.setIntercellSpacing(new Dimension(0, 0));
		luaCodeTable.setDefaultRenderer(Object.class, cellRender);		
	}

	public void setCode(String[] code){
		codeLines = code;
	}
	
	@SuppressWarnings("serial")
	class LuaCodeTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			if (codeLines == null)
				return 0;
			else
				return codeLines.length;
		}

		@Override
		public String getColumnName(int column) {
			return "";
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return " " + (rowIndex + 1) + " ";
			else
				return codeLines[rowIndex];
		}

	}
}
