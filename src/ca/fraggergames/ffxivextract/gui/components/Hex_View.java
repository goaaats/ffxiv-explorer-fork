package ca.fraggergames.ffxivextract.gui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

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
			
			byteToChar[i] = ""+(char) i;
		}
				
		txtHexData.setTableHeader(null);
		txtHexData.setGridColor(Color.GRAY);
		txtHexData.getColumnModel().getColumn(0).setMinWidth(70);
		
		DefaultTableCellRenderer cellRender = new DefaultTableCellRenderer() {
	        public Component getTableCellRendererComponent(JTable table, Object value,
	                         boolean isSelected, boolean hasFocus, int row, int column) {
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
	        	  setBorder(BorderFactory.createMatteBorder(0, 0, 1, 2, Color.LIGHT_GRAY));
	          else if (column == 16)
	        	  setBorder(BorderFactory.createMatteBorder(0, 0, 1, 2, Color.LIGHT_GRAY));
	          else
	        	  setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
	          	    
	          return this;
	        }
	      };
		txtHexData.setShowGrid(false);
		txtHexData.setIntercellSpacing(new Dimension(0, 0));
		txtHexData.setDefaultRenderer(Object.class, cellRender);
		
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
				return String.format("%x: ", 16 * rowIndex);
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
