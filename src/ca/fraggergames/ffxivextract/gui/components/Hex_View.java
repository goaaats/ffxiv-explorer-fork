package ca.fraggergames.ffxivextract.gui.components;

import java.awt.Color;
import java.awt.GridBagConstraints;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.AbstractTableModel;

import ca.fraggergames.ffxivextract.models.EXDF_File;
import ca.fraggergames.ffxivextract.models.EXDF_StringEntry;

@SuppressWarnings("serial")
public class Hex_View extends JScrollPane{

	JTable txtHexData = new JTable(new HexTableModel());
	int columnCount;
	byte[] bytes = null;
	String byteToStr[] = new String[256];
	String byteToChar[] = new String[256];
	
	public Hex_View(int columnCount)
	{		
		this.columnCount = columnCount;		
		
		setVerticalScrollBarPolicy ( ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS );
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL | GridBagConstraints.VERTICAL;
		getViewport().add(txtHexData);		
		
		for (int i = 0; i < 256; i++)
		{
			byteToStr[i] = String.format("%02X", i);
			
			if (i >= 32 && i <= 126)
				byteToChar[i] = ".";
			else
				byteToChar[i] = ""+(char) i;
		}
				
		txtHexData.setTableHeader(null);
		txtHexData.setGridColor(Color.GRAY);
	}
	
	public void setBytes(byte[] byteArray)
	{
		bytes = byteArray;
		((AbstractTableModel) txtHexData.getModel()).fireTableDataChanged();
	}
			
	class HexTableModel extends AbstractTableModel{

		
		
		public HexTableModel()
		{
			
		}
		
		@Override
		public int getColumnCount() {
			return 33;
		}

		@Override
		public int getRowCount() {
			if (bytes == null || bytes.length == 0)
				return 0;
			else
				return bytes.length / 16;
		}
		
		@Override
		public String getColumnName(int column) {
			return "";
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
			{
				return String.format("%x", 16 * rowIndex);
			}
			else if (columnIndex >= 1 && columnIndex <= 16)
			{
				int value = bytes[(rowIndex * 16) + columnIndex - 1];
				return byteToStr[value & 0xFF];
			}
			else
			{
				int value = bytes[(rowIndex * 16) + columnIndex - 17];
				return byteToChar[value & 0xFF];
			}
		}
		
	}
}
