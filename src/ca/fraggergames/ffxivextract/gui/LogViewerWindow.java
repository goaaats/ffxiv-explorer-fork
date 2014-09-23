package ca.fraggergames.ffxivextract.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ca.fraggergames.ffxivextract.models.Log_File.Log_Entry;

public class LogViewerWindow extends JFrame {

	private JPanel contentPane;
	private JTable table;
	
	/**
	 * Create the frame.
	 */
	public LogViewerWindow() {
		setTitle("Log Viewer");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 2));
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 0, 0, 0));
		contentPane.add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel = new JLabel("Filter: ");
		panel.add(lblNewLabel);
		
		JComboBox comboBox = new JComboBox();
		panel.add(comboBox);
		
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
				saveLog();
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
	}

	private void loadLog(){

		Log_Entry[] log = null;
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setModel(new LogTableModel(log));
		
		for (int column = 0; column < table.getColumnCount(); column++)
		{
		    TableColumn tableColumn = table.getColumnModel().getColumn(column);
		    int preferredWidth = tableColumn.getMinWidth();
		    int maxWidth = tableColumn.getMaxWidth();
		 
		    for (int row = 0; row < table.getRowCount(); row++)
		    {
		        TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
		        Component c = table.prepareRenderer(cellRenderer, row, column);
		        int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
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
	
	private void saveLog() {
		
	}
	
	@SuppressWarnings("serial")
	class LogTableModel extends AbstractTableModel{

		Log_Entry entries[];
		String[] columns = {"Time", "Channel", "Message"};
		
		public LogTableModel(Log_Entry[] file)
		{
			entries = file;
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
				return entries[rowIndex].time;
			else if (columnIndex == 1)
				return entries[rowIndex].channel;
			else if (columnIndex == 2)
				return entries[rowIndex].message.toString();
			else
				return "";
		}
		
	}
}
