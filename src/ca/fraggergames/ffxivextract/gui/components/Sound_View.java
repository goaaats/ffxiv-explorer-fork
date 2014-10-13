package ca.fraggergames.ffxivextract.gui.components;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;

import ca.fraggergames.ffxivextract.models.SCD_File;
import ca.fraggergames.ffxivextract.models.SCD_File.SCD_Sound_Info;
import javax.swing.JScrollPane;

public class Sound_View extends JPanel {
	private JTable tblSoundEntyList;

	public Sound_View(SCD_File scdFile) {
		setLayout(new BorderLayout(0, 0));

		JPanel pnlFileList = new JPanel();
		pnlFileList.setBorder(new TitledBorder(null, "SCD Contents",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		add(pnlFileList, BorderLayout.CENTER);
		pnlFileList.setLayout(new BoxLayout(pnlFileList, BoxLayout.X_AXIS));
		
		JScrollPane scrollPane = new JScrollPane();
		pnlFileList.add(scrollPane);
		
		tblSoundEntyList = new JTable();
		tblSoundEntyList.setShowVerticalLines(false);
		scrollPane.setViewportView(tblSoundEntyList);
		tblSoundEntyList.setModel(new SCDTableModel(scdFile));
		tblSoundEntyList.getColumnModel().getColumn(4).setPreferredWidth(79);
	}

	class SCDTableModel extends AbstractTableModel {

		SCD_File file;
		String[] columns = { "Index", "File Size", "Data Type", "Frequency",
				"Num Channels", "Loop Start", "Loop End" };

		public SCDTableModel(SCD_File file) {
			this.file = file;
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public int getRowCount() {
			return file.getNumEntries();
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {

			SCD_Sound_Info info = file.getSoundInfo(rowIndex);

			if (info == null) {
				if (columnIndex == 0)
					return rowIndex;
				else if (columnIndex == 1)
					return "Empty entry";
				else
					return "N/A";
			}

			if (columnIndex == 0)
				return rowIndex;
			else if (columnIndex == 1)
				return info.fileSize;
			else if (columnIndex == 2) {
				switch (info.dataType) {
				case 0x06:
					return "OGG";
				case 0x0C:
					return "MS-ADPCM";
				}
			} 
			else if (columnIndex == 3)
				return info.frequency;
			else if (columnIndex == 4)
				return info.numChannels;
			else if (columnIndex == 5)
				return info.loopStart;
			else if (columnIndex == 6)
				return info.loopEnd;
			else
				return "";
			return "";
		}

	}

}
