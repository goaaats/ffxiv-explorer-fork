package ca.fraggergames.ffxivextract.gui.components;

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

@SuppressWarnings("serial")
public class Lua_View extends JScrollPane {

	JTable luaCodeTable;
	String[] codeLines;

	public Lua_View() {
		luaCodeTable = new JTable(new LuaCodeTableModel());

		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL
				| GridBagConstraints.VERTICAL;
		getViewport().add(luaCodeTable);

		//Graphics Stuff		
		DefaultTableCellRenderer cellRender = new DefaultTableCellRenderer() {
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JTableHeader header = table.getTableHeader();
				if (header != null) {
					setForeground(Color.BLACK);
					setBackground(Color.BLACK);
					setFont(header.getFont());
				}				
				
				if (column == 0)
					setHorizontalAlignment(JLabel.RIGHT);
				else
					setHorizontalAlignment(JLabel.CENTER);
				
				setText((value == null) ? "" : value.toString());

				if (column == 0)
					setBorder(BorderFactory.createMatteBorder(0, 0, 0, 2,
							Color.LIGHT_GRAY));				

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
				return rowIndex;
			else
				return codeLines[rowIndex];
		}

	}
}
