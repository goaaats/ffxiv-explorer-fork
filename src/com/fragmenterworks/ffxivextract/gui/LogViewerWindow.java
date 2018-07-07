package com.fragmenterworks.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.fragmenterworks.ffxivextract.Strings;

import com.fragmenterworks.ffxivextract.models.Log_File;
import com.fragmenterworks.ffxivextract.models.Log_File.Log_Entry;

public class LogViewerWindow extends JFrame {

	public final int SAVETYPE_PLAIN = 0;
	public final int SAVETYPE_CSV = 1;

	private JPanel contentPane;
	private JTable table;

	private ArrayList<Log_Entry> logList = new ArrayList<Log_Entry>();

	public LogViewerWindow() {
		setTitle("Log Viewer");		
		URL imageURL = getClass().getResource("/res/frameicon.png");
		ImageIcon image = new ImageIcon(imageURL);
		this.setIconImage(image.getImage());
				
		setBounds(100, 100, 900, 600);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 2));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new EmptyBorder(10, 0, 10, 0));
		contentPane.add(panel_1, BorderLayout.CENTER);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane);

		table = new JTable();
		scrollPane.setViewportView(table);

		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.SOUTH);

		JButton btnSaveLog = new JButton("Save Log");
		btnSaveLog.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				FileFilter filterPlain = new FileFilter() {

					@Override
					public String getDescription() {
						return "Text Document (.txt)";
					}

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".txt");
					}
				};
				fileChooser.addChoosableFileFilter(filterPlain);
				FileFilter filterCSV = new FileFilter() {

					@Override
					public String getDescription() {
						return "Text CSV (.csv)";
					}

					@Override
					public boolean accept(File f) {
						return f.getName().endsWith(".csv");
					}
				};
				fileChooser.addChoosableFileFilter(filterPlain);
				fileChooser.addChoosableFileFilter(filterCSV);
				fileChooser.setFileFilter(filterPlain);
				fileChooser.setAcceptAllFileFilterUsed(false);

				int retunval = fileChooser
						.showSaveDialog(LogViewerWindow.this);

				if (retunval == JFileChooser.APPROVE_OPTION) {
					String path;
					try {						
						path = fileChooser.getSelectedFile().getCanonicalPath();
						if (fileChooser.getFileFilter().equals(filterPlain)) 
							path+=".txt"; 
						else 
							path+=".csv";
						saveLog(path, path.endsWith(".txt") ? SAVETYPE_PLAIN : SAVETYPE_CSV);
					} catch (IOException e1) {						
						e1.printStackTrace();
					}					
				}
			}
		});
		panel_2.add(btnSaveLog);

		JButton btnClose = new JButton("Close");
		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});

		panel_2.add(btnClose);
		
		loadLog();
	}

	private void loadLog() {
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		FileFilter filter = new FileFilter() {
			
			@Override
			public String getDescription() {
				return Strings.FILETYPE_FFXIV_LOG;
			}
			
			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".log") || f.isDirectory();
			}				
		};
		fileChooser.addChoosableFileFilter(filter);
		fileChooser.setFileFilter(filter);
		fileChooser.setAcceptAllFileFilterUsed(false);
		
		int retVal = fileChooser.showOpenDialog(this);
		
		Log_File logs[];
		
		if (retVal == JFileChooser.APPROVE_OPTION)
		{
			if (!fileChooser.getSelectedFile().getParentFile().getName().equals("log"))
			{
				JOptionPane.showMessageDialog(this, "This is not a valid log folder. Please point the path to the log folder in your \".\\documents\\my games\\FINAL FANTASY XIV - A Realm Reborn\\\" folder.", "Error opening logs",
					    JOptionPane.ERROR_MESSAGE);
				dispose();
				return;
			}
			
			File directory = fileChooser.getSelectedFile().getParentFile();
			File list[] = directory.listFiles();
			logs = new Log_File[list.length];
			
			for (int i = 0; i < logs.length; i++){
				try {
					logs[i] = new Log_File(list[i].getAbsolutePath());
					Log_Entry entries[] = logs[i].getEntries();
					
					for (int j = 0; j < entries.length; j++)
					{
						Log_Entry toAdd = entries[j];
						if (toAdd.filter == 0)
						logList.add(toAdd);
					}
					
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else
		{
			dispose();
			return;
		}		
		
		// Sort by time
		Collections.sort(logList, new Comparator<Log_Entry>() {
			public int compare(Log_Entry o1, Log_Entry o2) {
				return o1.time > o2.time ? 1 : (o1.time == o2.time ? 0 : -1);
			};
		});
		
		// Setup and load table
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(new LogTableModel(logList));

		for (int column = 0; column < table.getColumnCount(); column++) {
			TableColumn tableColumn = table.getColumnModel().getColumn(column);
			int preferredWidth = tableColumn.getMinWidth();
			int maxWidth = tableColumn.getMaxWidth();

			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer cellRenderer = table.getCellRenderer(row,
						column);
				Component c = table.prepareRenderer(cellRenderer, row, column);
				int width = c.getPreferredSize().width
						+ table.getIntercellSpacing().width;
				preferredWidth = Math.max(preferredWidth, width);

				// We've exceeded the maximum width, no need to check other rows

				if (preferredWidth >= maxWidth) {
					preferredWidth = maxWidth;
					break;
				}
			}

			tableColumn.setPreferredWidth(preferredWidth);
		}
	}

	private void saveLog(String path, int type) {
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(path));

			Iterator<Log_Entry> it = logList.iterator();

			if (SAVETYPE_CSV == type)
				bw.write("Time,Channel,Sender,Message\r\n");
			
			while (it.hasNext()) {
				Log_Entry entry = it.next();
				if (SAVETYPE_PLAIN == type)
					bw.write("[" + entry.formattedTime + "] " + " " + Log_File.getChannelName(entry.channel) + " : " + entry.sender
							+ " : " + entry.message + "\r\n");
				else if (SAVETYPE_CSV == type)
					bw.write("\""+entry.formattedTime + "\",\"" + Log_File.getChannelName(entry.channel) + "\",\"" + entry.sender + "\",\""
							+ entry.message + "\"\r\n");
			}
			bw.close();
		} catch (IOException e) {

		}
	}

	@SuppressWarnings("serial")
	class LogTableModel extends AbstractTableModel {

		ArrayList<Log_Entry> entries;
		String[] columns = { "Time", "Channel", "Sender", "Message" };

		public LogTableModel(ArrayList<Log_Entry> list) {
			entries = list;
		}

		@Override
		public int getColumnCount() {
			return columns.length;
		}

		@Override
		public int getRowCount() {
			return entries.size();
		}

		@Override
		public String getColumnName(int column) {
			return columns[column];
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0)
				return entries.get(rowIndex).formattedTime;
			else if (columnIndex == 1)
				return Log_File.getChannelName(entries.get(rowIndex).channel);
			else if (columnIndex == 2)
				return "<html>"+entries.get(rowIndex).sender.toString().replace("<", "&lt;").replace(">", "&gt;").replace("&lt;color #", "<font color=#").replace("&lt;/color&gt;", "</font>")+"</html>";
			else if (columnIndex == 3)
				return "<html>"+entries.get(rowIndex).message.toString().replace("<", "&lt;").replace(">", "&gt;").replace("&lt;color #", "<font color=#").replace("&lt;/color&gt;", "</font>")+"</html>";
			else
				return "";
		}

	}
}
