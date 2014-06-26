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
public class Hex_View extends JScrollPane {

	JTable txtHexData = null;
	int columnCount;
	byte[] bytes = null;
	String byteToStr[] = new String[256];
	String byteToChar[] = new String[256];

	public Hex_View(int columnCount) {
		this.columnCount = columnCount;

		txtHexData = new JTable(new HexTableModel());

		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		GridBagConstraints constraints = new GridBagConstraints();
		constraints.fill = GridBagConstraints.HORIZONTAL
				| GridBagConstraints.VERTICAL;
		getViewport().add(txtHexData);

		for (int i = 0; i < 256; i++) {
			byteToStr[i] = String.format("%02X", i);

			byteToChar[i] = "" + (char) i;
		}

		txtHexData.getColumnModel().getColumn(0).setMinWidth(70);

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
					setBorder(BorderFactory.createMatteBorder(0, 0, 1, 2,
							Color.LIGHT_GRAY));
				else if (column == Hex_View.this.columnCount) {
					if (value == null)
						setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0,
								Color.LIGHT_GRAY));
					else
						setBorder(BorderFactory.createMatteBorder(0, 0, 1, 2,
								Color.LIGHT_GRAY));
				} else {
					if (value == null)
						setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0,
								Color.LIGHT_GRAY));
					else
						setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1,
								Color.LIGHT_GRAY));
				}

				return this;
			}
		};
		txtHexData.setTableHeader(null);
		txtHexData.setShowGrid(false);
		txtHexData.setIntercellSpacing(new Dimension(0, 0));
		txtHexData.setDefaultRenderer(Object.class, cellRender);

	}

	public void setBytes(byte[] byteArray) {
		bytes = byteArray;
		((AbstractTableModel) txtHexData.getModel()).fireTableDataChanged();

	}

	class HexTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return columnCount + 1 + columnCount; // Address Column + 16 Bytes
													// of Hex + 16 Bytes of
													// Chars
		}

		@Override
		public int getRowCount() {
			if (bytes == null || bytes.length == 0)
				return 0;
			else {
				if (bytes.length % columnCount == 0)
					return (bytes.length / columnCount);
				else
					return (bytes.length / columnCount) + 1;
			}

		}

		@Override
		public String getColumnName(int column) {
			return "";
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return String.format("%x: ", columnCount * rowIndex);
			} else if (columnIndex >= 1 && columnIndex <= columnCount) {
				if (((rowIndex * columnCount) + columnIndex - 1) > bytes.length - 1)
					return null;

				int value = bytes[(rowIndex * columnCount) + columnIndex - 1];
				return byteToStr[value & 0xFF];
			} else {
				if (((rowIndex * columnCount) + columnIndex - columnCount - 1) > bytes.length - 1)
					return null;

				int value = bytes[(rowIndex * columnCount) + columnIndex
						- columnCount - 1];
				return byteToChar[value & 0xFF];
			}
		}

	}

}
