package ca.fraggergames.ffxivextract.gui.components;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import ca.fraggergames.ffxivextract.models.EXDF_File;
import ca.fraggergames.ffxivextract.models.EXDF_StringEntry;

@SuppressWarnings("serial")
public class EXDF_View extends JScrollPane{	
	
	JTable stringTable;
	
	public EXDF_View(EXDF_File file) {
		
		stringTable = new JTable(new EXDFTableModel(file));
		getViewport().add(stringTable);
		
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
