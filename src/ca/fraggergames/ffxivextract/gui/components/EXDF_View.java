package ca.fraggergames.ffxivextract.gui.components;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import ca.fraggergames.ffxivextract.helpers.FFXIV_String;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
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
	
	public static final String[] langs = {"en","ja", "fr", "de"};
	
	//EXH Context
	SqPack_IndexFile currentIndex;	
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
	
	private int langOverride = -1;
	
	//Given a EXD file, figure out EXH name, and look for it.
	public EXDF_View(SqPack_IndexFile currentIndex, String fullPath, EXDF_File file) {		
		
		this();
		
		this.currentIndex = currentIndex;
		
		fullPath = fullPath.toLowerCase();
		
		String exhName;
		
		//If the name is unknown, don't bother
		if (!fullPath.contains(".exd"))
		{
			setupUI_noExhFile();
			return;
		}		
		
		//Create the path to EXH				
		exhName = fullPath;
		exhName = exhName.replace("_en.exd", "");
		exhName = exhName.replace("_ja.exd", "");
		exhName = exhName.replace("_de.exd", "");
		exhName = exhName.replace("_fr.exd", "");
		exhName = exhName.replace("_chs.exd", "");
		exhName = exhName.substring(0, exhName.lastIndexOf("_"));
		String folderName = exhName.substring(0, fullPath.lastIndexOf("/"));
		exhName = exhName.substring(fullPath.lastIndexOf("/")+1, exhName.length()) +".exh";
		
		//Find this thing
		
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
					byte[] data = currentIndex.extractFile(currentIndex.getPackFolders()[folderIndex].getFiles()[j].getOffset(),  null);
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
	public EXDF_View(SqPack_IndexFile currentIndex, String fullPath, EXHF_File file) {		
		
		this();
		
		fullPath = fullPath.toLowerCase();
		
		this.currentIndex = currentIndex;
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
		
		///if (fullPath.contains("item"))
			//addAllWeaponModels();
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
							
							//Hey we accidently found something
							if (HashDatabase.getFileName(fileHash) == null){
								System.out.println("Adding: " + formattedExdName);
								if (numLanguages > 1)					
									HashDatabase.addPathToDB(String.format(exdName, exhFile.getPageTable()[i].pageNum, "_"+langs[j]));
								else
									HashDatabase.addPathToDB(String.format(exdName, exhFile.getPageTable()[i].pageNum, ""));
							}
							byte[] data = currentIndex.extractFile(currentIndex.getPackFolders()[folderIndex].getFiles()[j2].getOffset(), null);
							exdFile[(i*(numLanguages == 1 ? 1 : 4))+j] = new EXDF_File(data);
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
		lblExhNumEntries.setText(""+exhFile.getNumEntries() + ((exhFile.getNumEntries() == exhFile.getTrueNumEntries()? "": " (Page Sum: " +exhFile.getTrueNumEntries() + ")")));
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
		
			if (exhFile.getTrueNumEntries() > exhFile.getNumEntries())
				return exhFile.getNumEntries();
			else
				return exhFile.getTrueNumEntries();

		}

		@Override
		public String getColumnName(int column) {
			if (column == 0)
				return "Index";
			else
				return (column-1) + " ["+String.format("0x%x",exhFile.getDatasetTable()[column-1].type)+"]" + "["+String.format("0x%x",exhFile.getDatasetTable()[column-1].offset)+"]";
		}		
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			try{								
				int page = 0;
				
//				rowIndex += exhFile.getPageTable()[0].pageNum;
				
				//Find Page
				int totalRealEntries = 0;				
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
							
						/*
						if (rowIndex+exhFile.getPageTable()[0].pageNum >= exhFile.getPageTable()[i].pageNum)
							continue;
						else
						{
							page = i-1;
							break;
						}*/
						totalRealEntries += exhFile.getPageTable()[i].numEntries;						
						if (totalRealEntries > rowIndex)
						{
							page = i;
							totalRealEntries -= exhFile.getPageTable()[i].numEntries;
							break;
						}
					}
										
				}
				
				//Grab Data		
				totalRealEntries = 0;		
				for (int i = 0; i < page; i++)
				{															
					totalRealEntries += exdFiles[((numLanguages == 1? 1 : 4)*i) + (langOverride != -1 ? langOverride : cmbLanguage.getSelectedIndex())].getNumEntries();					
				}
				
				EXDF_Entry entry = exdFiles[((numLanguages == 1? 1 : 4)*page) + (langOverride != -1 ? langOverride : cmbLanguage.getSelectedIndex())].getEntry(rowIndex-totalRealEntries);
				
				//Index
				if (columnIndex == 0)
					return ""+entry.getIndex();
				
				//Data
				EXDF_Dataset dataset = exhFile.getDatasetTable()[columnIndex-1];									
				switch (dataset.type)
				{									
				case 0x0b: // QUAD
					int quad[] = entry.getQuad(dataset.offset);
					return quad[3] + ", " + quad[2] + ", " + quad[1] + ", " + quad[0];
				case 0x09: // FLOAT
				case 0x08:
					return entry.getFloat(dataset.offset);
				case 0x07: // INT
				case 0x06:  
					return entry.getInt(dataset.offset);
				case 0x05: // SHORT
				case 0x04:
					return ((int)entry.getShort(dataset.offset) & 0xFFFF);
				case 0x03: // BYTE
				case 0x02:  
					return (((int)entry.getByte(dataset.offset)) & 0xFF);	
				case 0x01: // BOOL
					return entry.getBoolean(dataset.offset);
				case 0x00: // STRING; Points to offset from end of dataset part. Read until 0x0.
					//return new String(entry.getString(exhFile.getDatasetChunkSize(), dataset.offset));
					return FFXIV_String.parseFFXIVString(entry.getString(exhFile.getDatasetChunkSize(), dataset.offset));				
				default:
					return "?";// Value: " + ((int)entry.getByte(dataset.offset)&0xFF);
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

	public boolean isSame(String name) {
		if (exhName == null || name == null)
			return false;
		if (name.contains(".exh"))
			return exhName.equals(name);
		if (!name.contains(".exd"))
			return false;
		String checkString = name; 
		checkString = checkString.replace("_en.exd", "");
		checkString = checkString.replace("_ja.exd", "");
		checkString = checkString.replace("_de.exd", "");
		checkString = checkString.replace("_fr.exd", "");
		checkString = checkString.replace("_chs.exd", "");
		checkString = checkString.substring(0, checkString.lastIndexOf("_")) +".exh";
		return exhName.equals(checkString);			
	}

	public void addAllWeaponModels()
	{
		//Write data
		for (int row = 0; row < table.getRowCount(); row++) {
			
			byte slot = (Byte) table.getValueAt(row, 48);
			String model1String[] = ((String) table.getValueAt(row, 11)).split(",");
			int model1[] = new int[model1String.length];
			for (int i = 0; i < model1.length; i++)
				model1[i] = Integer.parseInt(model1String[i].trim());
			String model2[] = ((String) table.getValueAt(row, 12)).split(",");
			
			String path = null, path2 = null;			
						
			switch(slot)
			{
			case 13: //Weapon
			case 2:
				HashDatabase.addPathToDB(String.format("chara/weapon/w%04d/obj/body/b%04d/model/w%04db%04d.mdl", model1[0], model1[1], model1[0] ,model1[1]));
				break;
			case 3: //Equipment
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0201e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0301e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0401e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0501e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0601e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0701e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0801e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0901e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1001e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1101e%04d_%s.mdl", model1[0], model1[0], "met"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1201e%04d_%s.mdl", model1[0], model1[0], "met"));
				break;
			case 4:
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0201e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0301e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0401e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0501e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0601e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0701e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0801e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0901e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1001e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1101e%04d_%s.mdl", model1[0], model1[0], "top"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1201e%04d_%s.mdl", model1[0], model1[0], "top"));
				break;
			case 5:
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0201e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0301e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0401e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0501e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0601e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0701e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0801e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0901e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1001e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1101e%04d_%s.mdl", model1[0], model1[0], "glv"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1201e%04d_%s.mdl", model1[0], model1[0], "glv"));
				break;
			case 6:
				break;
			case 7:
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0201e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0301e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0401e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0501e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0601e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0701e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0801e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0901e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1001e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1101e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1201e%04d_%s.mdl", model1[0], model1[0], "dwn"));
				break;
			case 8:
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "met"));
				break;				
			case 9: //Accessory
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "ear"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "ear"));
				break;
			case 10:
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "nek"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "nek"));
				break;
			case 11:
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "wrs"));
				break;
			case 12:									
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "rir"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "ril"));
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "ril"));
				break;
			default: continue;
			}
			
			if (path != null){
				System.out.println("Adding " + path);
				HashDatabase.addPathToDB(path);
				if (path2 != null)
					HashDatabase.addPathToDB(path2);
			}
		}
	}
	
	public void saveCSV(String path, int lang) throws IOException
	{
		langOverride = lang;
		OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(path),"UTF-8");		
		
		//Write columns
		for (int col = 0; col < table.getColumnCount(); col++)
		{
			out.write(table.getColumnName(col));
			if (col != table.getColumnCount()-1)
				out.write(",");
		}
		
		out.write("\r\n");
		
		//Write data
		for (int row = 0; row < table.getRowCount(); row++) {			
		    for (int col = 0; col < table.getColumnCount(); col++) {
		    	Object value = table.getValueAt(row, col);
		    	if (value instanceof String)
		    		out.write("\""+(String) value+"\""); 
		    	else 
		    		out.write("" + value);
		    	if (col != table.getColumnCount()-1)
		    		out.write(",");
		    }
		    out.write("\r\n");
		}
		out.close();
		langOverride = -1;
	}

	public int getNumLangs() {
		return exhFile.getNumLanguages();
	}	
}
