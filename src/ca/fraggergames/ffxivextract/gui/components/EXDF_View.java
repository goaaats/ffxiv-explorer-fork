package ca.fraggergames.ffxivextract.gui.components;

import java.awt.Component;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ca.fraggergames.ffxivextract.models.EXDF_File;
import ca.fraggergames.ffxivextract.models.EXDF_StringEntry;

@SuppressWarnings("serial")
public class EXDF_View extends JScrollPane{	
	
	JTable stringTable;
	
	public EXDF_View(EXDF_File file) {		
		
		stringTable = new JTable(new EXDFTableModel(file));
		stringTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		getViewport().add(stringTable);
		
		for (int column = 0; column < stringTable.getColumnCount(); column++)
		{
		    TableColumn tableColumn = stringTable.getColumnModel().getColumn(column);
		    int preferredWidth = tableColumn.getMinWidth();
		    int maxWidth = tableColumn.getMaxWidth();
		 
		    for (int row = 0; row < stringTable.getRowCount(); row++)
		    {
		        TableCellRenderer cellRenderer = stringTable.getCellRenderer(row, column);
		        Component c = stringTable.prepareRenderer(cellRenderer, row, column);
		        int width = c.getPreferredSize().width + stringTable.getIntercellSpacing().width;
		        preferredWidth = Math.max(preferredWidth, width);
		 
		        //  We've exceeded the maximum width, no need to check other rows
		 
		        if (preferredWidth >= maxWidth)
		        {
		            preferredWidth = maxWidth;
		            break;
		        }
		    }
		 
		    tableColumn.setPreferredWidth( preferredWidth );
		}
	}

	class EXDFTableModel extends AbstractTableModel{

		EXDF_StringEntry[] entries;
		String[] columns = {"Name", "Value"};
		
		public EXDFTableModel(EXDF_File file)
		{
			entries = file.strings;			
		}
		
		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public int getRowCount() {
			return entries.length;
		}
		
		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return entries[rowIndex].name;
			else if (columnIndex == 1)
				return entries[rowIndex].value;
			else
				return "";
		}
		
	}
}
