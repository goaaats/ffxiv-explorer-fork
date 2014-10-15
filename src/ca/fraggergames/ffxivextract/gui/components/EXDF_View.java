package ca.fraggergames.ffxivextract.gui.components;

import java.io.IOException;
import java.util.Hashtable;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import ca.fraggergames.ffxivextract.models.EXDF_File;
import ca.fraggergames.ffxivextract.models.EXDF_File.EXDF_Entry;
import ca.fraggergames.ffxivextract.models.EXHF_File.EXDF_Dataset;
import ca.fraggergames.ffxivextract.models.EXHF_File;
import ca.fraggergames.ffxivextract.models.SqPack_DatFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.storage.HashDatabase;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.border.TitledBorder;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import java.awt.Component;
import javax.swing.border.CompoundBorder;
import javax.swing.UIManager;
import java.awt.Color;
import javax.swing.border.EmptyBorder;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.JComboBox;
import javax.swing.SwingConstants;
import javax.swing.DefaultComboBoxModel;

@SuppressWarnings("serial")
public class EXDF_View extends JScrollPane implements ItemListener{	
	
	private static final String[] langs = {"en","ja", "fr", "de"};
	
	//EXH Context
	SqPack_IndexFile currentIndex;
	SqPack_DatFile currentDat;
	EXHF_File exhFile = null;
	EXDF_File exdFile[] = null;
	String exhFolder;
	String exhName;
	
	//To speed things up
	private int folderIndex = 0;	
	private int numPages = -1;
	private int numLanguages = -1;
	
	//UI
	private JLabel lblExhName;	
	private JLabel lblExhNumEntries;	
	private JLabel lblExhNumPages;	
	private JLabel lblExhNumLangs;
	private JComboBox cmbLanguage;
	private JTable table;
	
	//Given a EXD file, figure out EXH name, and look for it.
	public EXDF_View(SqPack_IndexFile currentIndex, SqPack_DatFile currentDat, String fullPath, EXDF_File file) {		
		
		this();
		
		this.currentIndex = currentIndex;
		this.currentDat = currentDat;
		
		String exhName;
		
		//If the name is unknown, don't bother
		if (!fullPath.contains(".exd"))
		{
			setupUI_noExhFile();
			return;
		}
		
		//Create the path to EXH				
		exhName = fullPath;
		exhName = exhName.replace(".exd", "");		
		exhName = exhName.substring(0, exhName.indexOf('_'));
		exhName = exhName.substring(exhName.lastIndexOf("/")+1, exhName.length());
		exhName += ".exh";
		
		//Find this thing
		String folderName = fullPath.substring(0, fullPath.lastIndexOf("/"));
		
		int folderHash = HashDatabase.computeCRC(folderName.getBytes(), 0, folderName.getBytes().length);
		int fullPathHash = HashDatabase.computeCRC(exhName.getBytes(), 0, exhName.getBytes().length);
		
		if (currentIndex.getPackFolders().length == 1)	
			folderIndex = 0;
		else
		{
			for (int i = 0; i < currentIndex.getPackFolders().length; i++)
			{
				if (currentIndex.getPackFolders()[i].getId() == folderHash)
				{
					folderIndex = i;
					break;
				}
			}
		}
		
		for (int j = 0; j < currentIndex.getPackFolders()[folderIndex].getFiles().length; j++)
		{
			//Found it
			if (currentIndex.getPackFolders()[folderIndex].getFiles()[j].getId() == fullPathHash)
			{
				try {
					byte[] data = currentDat.extractFile(currentIndex.getPackFolders()[folderIndex].getFiles()[j].getOffset(), null);
					exhFile = new EXHF_File(data);
					break;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		//No EXH file found...
		if (exhFile == null)
		{
			setupUI_noExhFile();
			return;
		}
		
		this.exhName = exhName; 
		
		//Init num language and num pages
		numPages = exhFile.getNumPages();
		numLanguages = exhFile.getNumLanguages();
		
		//Create the path to EXD
		String parsedExdName = exhName;
		parsedExdName = exhName.replace(".exh", "");
		parsedExdName += "_%s%s.exd"; // name_0_en.exd		
		
		getEXDFiles(exhFile, parsedExdName, numPages, numLanguages);
		
		setupUI();
	}

	//Given a EXH file, figure out EXD name, and look for it.
	public EXDF_View(SqPack_IndexFile currentIndex, SqPack_DatFile currentDat, String fullPath, EXHF_File file) {		
		
		this();
		
		this.currentIndex = currentIndex;
		this.currentDat = currentDat;
		this.exhFile = file;		
		
		//If the name is unknown, don't bother
		if (!fullPath.contains(".exh"))
		{
			setupUI_noExhFile();
			return;
		}
		
		//Init num language and num pages
		numPages = exhFile.getNumPages();
		numLanguages = exhFile.getNumLanguages();
		
		this.exhName = fullPath.substring(fullPath.lastIndexOf("/")+1, fullPath.length());
		
		//Create the path to EXD
		String exdName = fullPath;
		exdName = fullPath.replace(".exh", "");
		exdName += "_%s%s.exd"; // name_0_en.exd		
		
		String folderName = fullPath.substring(0, fullPath.lastIndexOf("/"));
		int folderHash = HashDatabase.computeCRC(folderName.getBytes(), 0, folderName.getBytes().length);
		
		if (currentIndex.getPackFolders().length == 1)	
			folderIndex = 0;
		else
		{
			for (int i = 0; i < currentIndex.getPackFolders().length; i++)
			{
				if (currentIndex.getPackFolders()[i].getId() == folderHash)
				{
					folderIndex = i;
					break;
				}
			}
		}
		
		getEXDFiles(exhFile, exdName, numPages, numLanguages);
		
		setupUI();
	}
	
	public EXDF_View()
	{
		
		JPanel panel = new JPanel();
		setViewportView(panel);
		panel.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new CompoundBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "EXH Header", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 10, 5, 10)));
		panel.add(panel_1, BorderLayout.NORTH);
		panel_1.setLayout(new BorderLayout(0, 0));
		
		JPanel panel_7 = new JPanel();
		panel_1.add(panel_7, BorderLayout.WEST);
		panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.Y_AXIS));
		
		JPanel panel_3 = new JPanel();
		panel_7.add(panel_3);
		panel_3.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_3.setLayout(new BoxLayout(panel_3, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel = new JLabel("EXH Name: ");
		panel_3.add(lblNewLabel);
		
		lblExhName = new JLabel("32");
		lblExhName.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel_3.add(lblExhName);
		
		JPanel panel_4 = new JPanel();
		panel_7.add(panel_4);
		panel_4.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_4.setLayout(new BoxLayout(panel_4, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_1 = new JLabel("Num Entries: ");
		panel_4.add(lblNewLabel_1);
		
		lblExhNumEntries = new JLabel("32");
		panel_4.add(lblExhNumEntries);
		
		JPanel panel_5 = new JPanel();
		panel_7.add(panel_5);
		panel_5.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_5.setLayout(new BoxLayout(panel_5, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_2 = new JLabel("Num Pages: ");
		panel_5.add(lblNewLabel_2);
		
		lblExhNumPages = new JLabel("32");
		panel_5.add(lblExhNumPages);
		
		JPanel panel_6 = new JPanel();
		panel_7.add(panel_6);
		panel_6.setAlignmentX(Component.LEFT_ALIGNMENT);
		panel_6.setLayout(new BoxLayout(panel_6, BoxLayout.X_AXIS));
		
		JLabel lblNewLabel_3 = new JLabel("Num Languages: ");
		panel_6.add(lblNewLabel_3);
		
		lblExhNumLangs = new JLabel("32");
		panel_6.add(lblExhNumLangs);
		
		JPanel panel_8 = new JPanel();
		panel_8.setBorder(null);
		panel_1.add(panel_8, BorderLayout.EAST);
		panel_8.setLayout(new BoxLayout(panel_8, BoxLayout.Y_AXIS));
		
		JPanel panel_9 = new JPanel();
		panel_9.setAlignmentY(0.0f);
		panel_9.setBorder(null);
		panel_8.add(panel_9);
		panel_9.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 0));
		
		JLabel lblNewLabel_4 = new JLabel("Language: ");
		lblNewLabel_4.setVerticalAlignment(SwingConstants.TOP);
		panel_9.add(lblNewLabel_4);
		
		cmbLanguage = new JComboBox();
		cmbLanguage.setModel(new DefaultComboBoxModel(new String[] {"N/A"}));
		cmbLanguage.setSelectedIndex(0);
		panel_9.add(cmbLanguage);
		
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new TitledBorder(null, "EXD Contents", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel.add(panel_2);
		panel_2.setLayout(new BorderLayout(0, 0));
		
		JScrollPane scrollPane = new JScrollPane();
		panel_2.add(scrollPane, BorderLayout.CENTER);
		
		table = new JTable();		
		scrollPane.setViewportView(table);
		
		
	}

	//Given a exd name, find all related exds (by page/language)
	private void getEXDFiles(EXHF_File exhFile, String exdName, int numPages, int numLanguages)
	{
		exdFile = new EXDF_File[numPages * 4];
		for (int i = 0; i < numPages; i++){
		
			for (int j = 0; j < numLanguages; j++){
				
				if (j>= 4)
					break;
				
				String formattedExdName = exdName;
				
				if (numLanguages > 1)					
					formattedExdName = String.format(exdName, exhFile.getPageTable()[i].pageNum, "_"+langs[j]);
				else
					formattedExdName = String.format(exdName, exhFile.getPageTable()[i].pageNum, "");
				
				formattedExdName = formattedExdName.substring(formattedExdName.lastIndexOf("/")+1);
				
				int fileHash = HashDatabase.computeCRC(formattedExdName.getBytes(), 0, formattedExdName.getBytes().length);
				
				//Find File		
				for (int j2 = 0; j2 < currentIndex.getPackFolders()[folderIndex].getFiles().length; j2++)
				{
					//Found it
					if (currentIndex.getPackFolders()[folderIndex].getFiles()[j2].getId() == fileHash)
					{
						try {
							byte[] data = currentDat.extractFile(currentIndex.getPackFolders()[folderIndex].getFiles()[j2].getOffset(), null);
							exdFile[(i*(numLanguages-1))+j] = new EXDF_File(data);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				
			}
		}
		
	}

	//Setup UI with known data
	private void setupUI() {
		lblExhName.setText(exhName);
		lblExhNumEntries.setText(""+exhFile.getNumEntries());
		lblExhNumLangs.setText(""+(exhFile.getNumLanguages()-1));
		lblExhNumPages.setText(""+exhFile.getNumPages());
		if (exhFile.getNumLanguages() != 1)
		{
			cmbLanguage.setModel(new DefaultComboBoxModel(new String[] {"English", "Japanese", "French", "German"}));
			cmbLanguage.addItemListener(this);
		}
		else
		{
			cmbLanguage.setModel(new DefaultComboBoxModel(new String[] {"N/A"}));
			cmbLanguage.setEnabled(false);
		}
		table.setModel(new EXDTableModel(exhFile, exdFile));
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	}
	
	//Setup UI to complain that the EXH file was not found
	private void setupUI_noExhFile() {
		lblExhName.setText("EXH FILE NOT FOUND");		
		lblExhName.setForeground(Color.RED);
		lblExhNumEntries.setText("N/A");
		lblExhNumLangs.setText("N/A");
		lblExhNumPages.setText("N/A");
		cmbLanguage.setModel(new DefaultComboBoxModel(new String[] {"N/A"}));
	}
	
	class EXDTableModel extends AbstractTableModel {

		EXHF_File exhFile;
		EXDF_File exdFiles[];
		
		public EXDTableModel(EXHF_File exh, EXDF_File[] exd) {
			this.exhFile = exh;
			this.exdFiles = exd;
		}

		@Override
		public int getColumnCount() {
			return exhFile.getDatasetTable().length + 1;
		}

		@Override
		public int getRowCount() {
		
			return exhFile.getNumEntries();

		}

		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Index";
			else
				return (column-1) + " ["+String.format("0x%x",exhFile.getDatasetTable()[column-1].type)+"]";
		}

		@Override
		public String getValueAt(int rowIndex, int columnIndex) {
			try{
				if (columnIndex == 0)
					return ""+rowIndex;
				
				int page = 0;
				
				//Find Page
				if (numPages != 1)
				{
					for (int i = 0; i <= exhFile.getPageTable().length; i++)
					{
						if (i == exhFile.getPageTable().length)
						{
							if (i <= exhFile.getPageTable()[i-1].pageNum + exhFile.getPageTable()[i-1].numEntries)
							{
								page = i-1;
								break;
							}
							else
								return "ERROR";
						}
							
						
						if (rowIndex >= exhFile.getPageTable()[i].pageNum)
							continue;
						else
						{
							page = i-1;
							break;
						}
					}
				}
				
				//Grab Data
				EXDF_Entry entry = exdFiles[((numLanguages == 1? 1 : numLanguages-1)*page) + cmbLanguage.getSelectedIndex()].getEntry(rowIndex);			
				EXDF_Dataset dataset = exhFile.getDatasetTable()[columnIndex-1];			
							
				switch (dataset.type)
				{
				case 0x04:
					return ""+entry.getShort(dataset.offset);
				case 0x03:				
				case 0x02:
					return ""+entry.getByte(dataset.offset);
				case 0x19:
				case 0x07:
				case 0x06:
					return ""+entry.getInt(dataset.offset);
				case 0x00: //String; Points to offset from end of dataset part. Read until 0x0.
					return entry.getString(exhFile.getDatasetChunkSize(), dataset.offset);
				default:
					return "?";
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
				return "";
			}
		}

	}

	@Override
	public void itemStateChanged(ItemEvent event) {
		if (event.getStateChange() == ItemEvent.SELECTED) {
	          ((EXDTableModel)table.getModel()).fireTableDataChanged();
	       }
	}
	
}
