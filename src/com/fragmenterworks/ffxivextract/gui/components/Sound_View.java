package com.fragmenterworks.ffxivextract.gui.components;

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import com.fragmenterworks.ffxivextract.helpers.MSADPCM_Decode;
import com.fragmenterworks.ffxivextract.models.SCD_File;
import com.fragmenterworks.ffxivextract.models.SCD_File.SCD_Sound_Info;

public class Sound_View extends JPanel {
	private JTable tblSoundEntyList;
	SCD_File file;
	
	//OggVorbisPlayer currentlyPlayingSong;
	
	public Sound_View(SCD_File scdFile) {		
		
		setLayout(new BorderLayout(0, 0));
		
		file = scdFile;
		
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
		tblSoundEntyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblSoundEntyList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent e) {
				
				
				if (!e.getValueIsAdjusting()){
			
					SCD_Sound_Info info = file.getSoundInfo(tblSoundEntyList.getSelectedRow());
					if (info != null)
					{
						if (info.dataType == 0x0C){
							final byte[] header = file.getADPCMHeader(tblSoundEntyList.getSelectedRow());
							final byte[] body = file.getADPCMData(tblSoundEntyList.getSelectedRow());
							new Thread() {
								
								@Override
								public void run() {
									play(header, body);
								}
							}.start();						
						}
						else
						{
					
						}
					}
				}
			}
		});
		
		this.addComponentListener(new ComponentListener() {
			
			@Override
			public void componentShown(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentResized(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentMoved(ComponentEvent arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void componentHidden(ComponentEvent arg0) {
			}
		});
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

	public void play(byte[] header, byte[] body)
	{		
		if (header == null || body == null)
			return;
		
		ByteBuffer bb = ByteBuffer.wrap(header);
		bb.order(ByteOrder.LITTLE_ENDIAN);
		bb.getShort();		
		int channels = bb.getShort();
		int rate = bb.getInt();
		bb.getInt();
		int blockAlign = bb.getShort();
		int bitsPerSample = bb.getShort();
		
		// Creates an AudioFormat object and a DataLine.Info object.
		AudioFormat audioFormat = new AudioFormat((float) rate, 16, channels,
				true, false);
		DataLine.Info datalineInfo = new DataLine.Info(SourceDataLine.class,
				audioFormat, AudioSystem.NOT_SPECIFIED);

		SourceDataLine outputLine;
		
		// Check if the line is supported.
		if (!AudioSystem.isLineSupported(datalineInfo)) {
			System.err.println("Audio output line is not supported.");
			return;
		}

		/*
		 * Everything seems to be alright. Let's try to open a line with the
		 * specified format and start the source data line.
		 */
		try {
			outputLine = (SourceDataLine) AudioSystem.getLine(datalineInfo);
			outputLine.open(audioFormat);
		} catch (LineUnavailableException exception) {
			System.out.println("The audio output line could not be opened due "
					+ "to resource restrictions.");
			System.err.println(exception);
			return;
		} catch (IllegalStateException exception) {
			System.out.println("The audio output line is already open.");
			System.err.println(exception);
			return;
		} catch (SecurityException exception) {
			System.out.println("The audio output line could not be opened due "
					+ "to security restrictions.");
			System.err.println(exception);
			return;
		}

		// Start it.
		outputLine.start();
		
		int bufferSize = MSADPCM_Decode.getBufferSize(body.length, channels, blockAlign, bitsPerSample);
		
		if (bufferSize % 4 != 0)
			bufferSize+=bufferSize % 4;
		
		byte outputBuffer[] = new byte[bufferSize];
		
		MSADPCM_Decode.decode(body, outputBuffer, body.length, channels, blockAlign);
		
		outputLine.write(outputBuffer, 0, outputBuffer.length);	
		
		outputLine.close();
	}
	
}
