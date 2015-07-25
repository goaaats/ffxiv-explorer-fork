package com.fragmenterworks.ffxivextract.gui.components;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.JScrollPane;
import javax.swing.JTable;

import com.fragmenterworks.ffxivextract.helpers.FFXIV_String;
import com.fragmenterworks.ffxivextract.helpers.SparseArray;
import com.fragmenterworks.ffxivextract.models.EXDF_File;
import com.fragmenterworks.ffxivextract.models.EXDF_File.EXDF_Entry;
import com.fragmenterworks.ffxivextract.models.EXHF_File.EXDF_Dataset;
import com.fragmenterworks.ffxivextract.models.EXHF_File;
import com.fragmenterworks.ffxivextract.models.SqPack_IndexFile;

import javax.swing.JPanel;

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

import com.fragmenterworks.ffxivextract.Constants;
import com.fragmenterworks.ffxivextract.storage.HashDatabase;

@SuppressWarnings("serial")
public class EXDF_View extends JScrollPane implements ItemListener{	
		
	//EXH Context
	SqPack_IndexFile currentIndex;	
	EXHF_File exhFile = null;
	EXDF_File exdFile[] = null;
	String exhFolder;
	String exhName;
	
	//To speed things up
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
	
	private SparseArray<String> columnNames = new SparseArray<String>();
	
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
		exhName = exhName.replace("_cht.exd", "");
		exhName = exhName.replace("_ko.exd", "");
		exhName = exhName.substring(0, exhName.lastIndexOf("_"));
		String folderName = exhName.substring(0, fullPath.lastIndexOf("/"));
		exhName = exhName.substring(fullPath.lastIndexOf("/")+1, exhName.length()) +".exh";
		
		exhFolder = folderName;
		
		//Find this thing				
		try {
				byte[] data = currentIndex.extractFile(folderName, exhName);
				
				if (data != null)
					exhFile = new EXHF_File(data);				
			} catch (IOException e) {
				e.printStackTrace();
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
		exhFolder = fullPath.substring(0, fullPath.lastIndexOf("/"));
		
		//Create the path to EXD
		String exdName = fullPath;
		exdName = fullPath.replace(".exh", "");
		exdName += "_%s%s.exd"; // name_0_en.exd				
		
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
		exdFile = new EXDF_File[exhFile.getNumPages()*exhFile.getNumLanguages()];
		for (int i = 0; i < numPages; i++){
		
			for (int j = 0; j < numLanguages; j++){							
				
				if (EXHF_File.languageCodes[exhFile.getLanguageTable()[j]].equals("Unknown"))
					continue;
				
				String formattedExdName = exdName;
								
				formattedExdName = String.format(exdName, exhFile.getPageTable()[i].pageNum, EXHF_File.languageCodes[exhFile.getLanguageTable()[j]]);
				
				formattedExdName = formattedExdName.substring(formattedExdName.lastIndexOf("/")+1);
				
				
				try {						
					//Hey we accidently found something
					if (HashDatabase.getFileName(HashDatabase.computeCRC(formattedExdName.getBytes(), 0, formattedExdName.getBytes().length)) == null){
						System.out.println("Adding: " + formattedExdName);
						
						HashDatabase.addPathToDB(exhFolder +"/"+formattedExdName, currentIndex.getIndexName());					
					}
					byte[] data = currentIndex.extractFile(exhFolder, formattedExdName);
					
					if (exdFile != null && data != null)
						exdFile[(i*numLanguages)+j] = new EXDF_File(data);
				} catch (IOException e) {
					e.printStackTrace();
				}
									
				
			}
		}
		
	}

	//Setup UI with known data
	private void setupUI() {

		loadColumnNames(exhName);
		
		table.setModel(new EXDTableModel(exhFile, exdFile));
		table.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
		
		lblExhName.setText(exhName);
		lblExhNumEntries.setText(""+exhFile.getNumEntries() + ((exhFile.getNumEntries() == exhFile.getTrueNumEntries()? "": " (Page Sum: " +exhFile.getTrueNumEntries() + ")")));
		lblExhNumLangs.setText("" + (exhFile.getLanguageTable()[0] == 0x0 ? 0 : exhFile.getNumLanguages()));
		lblExhNumPages.setText(""+exhFile.getNumPages());
		
		cmbLanguage.removeAllItems();
		if (exhFile.getNumLanguages() != 0 && exhFile.getLanguageTable()[0] != 0x0)
		{
			for (int i = 0; i < numLanguages; i++)
				cmbLanguage.addItem(EXHF_File.languageNames[exhFile.getLanguageTable()[i]]);			
			cmbLanguage.addItemListener(this);
		}
		else
		{
			cmbLanguage.setModel(new DefaultComboBoxModel(new String[] {"N/A"}));
			cmbLanguage.setEnabled(false);
		}

		cmbLanguage.setSelectedIndex(Constants.defaultLanguage > exhFile.getNumLanguages()-1 ? 0 : Constants.defaultLanguage);			
		
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
				return columnNames.get(column-1, (column-1) + " ["+String.format("0x%x",exhFile.getDatasetTable()[column-1].type)+"]" + "["+String.format("0x%x",exhFile.getDatasetTable()[column-1].offset)+"]");
		}		
		
		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			try{								
				int page = 0;
				
//				rowIndex += exhFile.getPageTable()[0].pageNum;
				
				//Check if we got data for this langauge
				if (exdFile[(langOverride != -1 ? langOverride : cmbLanguage.getSelectedIndex())] == null)
				{
					return "";
				}
				
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
					totalRealEntries += exdFiles[(numLanguages*i) + (langOverride != -1 ? langOverride : cmbLanguage.getSelectedIndex())].getNumEntries();					
				}
				
				EXDF_Entry entry = exdFiles[(numLanguages*page) + (langOverride != -1 ? langOverride : cmbLanguage.getSelectedIndex())].getEntry(rowIndex-totalRealEntries);
				
				//Index
				if (columnIndex == 0)
					return entry.getIndex();
				
				//Data
				EXDF_Dataset dataset = exhFile.getDatasetTable()[columnIndex-1];
				
				//Special case for byte bools
				if (dataset.type >= 0x19)
				{
					boolean bool = entry.getByteBool(dataset.type, dataset.offset);
					return bool;
				}
				else
				{
					switch (dataset.type)
					{									
					case 0x0b: // QUAD
						int quad[] = entry.getQuad(dataset.offset);
						return quad[3] + ", " + quad[2] + ", " + quad[1] + ", " + quad[0];
					case 0x09: // FLOAT
					//case 0x08:
						return entry.getFloat(dataset.offset);
					case 0x07: // UINT
						return (long)entry.getInt(dataset.offset) & 0xFFFFFFFF;
					case 0x06: // INT 
						return entry.getInt(dataset.offset);
					case 0x05: // USHORT
						return ((int)entry.getShort(dataset.offset) & 0xFFFF);
					case 0x04: // SHORT
						return entry.getShort(dataset.offset);
					case 0x03: // UBYTE
						return (((int)entry.getByte(dataset.offset)) & 0xFF);
					case 0x02: // BYTE
						return entry.getByte(dataset.offset);	
					case 0x01: // BOOL
						return entry.getBoolean(dataset.offset);
					case 0x00: // STRING; Points to offset from end of dataset part. Read until 0x0.
						//return new String(entry.getString(exhFile.getDatasetChunkSize(), dataset.offset));
						return FFXIV_String.parseFFXIVString(entry.getString(exhFile.getDatasetChunkSize(), dataset.offset));				
					default:
						return "?";// Value: " + ((int)entry.getByte(dataset.offset)&0xFF);
					}
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
		checkString = checkString.replace("_cht.exd", "");
		checkString = checkString.replace("_ko.exd", "");
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
				HashDatabase.addPathToDB(String.format("chara/weapon/w%04d/obj/body/b%04d/model/w%04db%04d.mdl", model1[0], model1[1], model1[0] ,model1[1]),"040000");
				break;
			case 3: //Equipment
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0201e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0301e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0401e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0501e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0601e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0701e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0801e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0901e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1001e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1101e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1201e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				break;
			case 4:
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0201e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0301e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0401e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0501e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0601e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0701e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0801e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0901e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1001e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1101e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1201e%04d_%s.mdl", model1[0], model1[0], "top"),"040000");
				break;
			case 5:
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0201e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0301e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0401e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0501e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0601e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0701e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0801e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0901e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1001e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1101e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1201e%04d_%s.mdl", model1[0], model1[0], "glv"),"040000");
				break;
			case 6:
				break;
			case 7:
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0201e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0301e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0401e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0501e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0601e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0701e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0801e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0901e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1001e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1101e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c1201e%04d_%s.mdl", model1[0], model1[0], "dwn"),"040000");
				break;
			case 8:
				HashDatabase.addPathToDB(String.format("chara/equipment/e%04d/model/c0101e%04d_%s.mdl", model1[0], model1[0], "met"),"040000");
				break;				
			case 9: //Accessory
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "ear"),"040000");
				break;
			case 10:
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "nek"),"040000");
				break;
			case 11:
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				HashDatabase.addPathToDB(String.format("chara/equipment/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "wrs"),"040000");
				break;
			case 12:									
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "rir"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0101a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0201a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0301a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0401a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0501a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0601a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0701a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0801a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c0901a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1001a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1101a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				HashDatabase.addPathToDB(String.format("chara/accessory/a%04d/model/c1201a%04d_%s.mdl", model1[0], model1[0], "ril"),"040000");
				break;
			default: continue;
			}
			
			if (path != null){
				System.out.println("Adding " + path);
				HashDatabase.addPathToDB(path,"040000");
				if (path2 != null)
					HashDatabase.addPathToDB(path2,"040000");
			}
		}
	}
	
	public void saveCSV(String path, int lang) throws IOException
	{
		langOverride = lang;
		
		//Skip this if langauge doesn't exist
		if (exdFile[(langOverride != -1 ? langOverride : cmbLanguage.getSelectedIndex())] == null)
			return;
		
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
		    	{
		    		String string = (String)value;
		    		string = string.replace("\"", "\"\"");
		    		out.write("\""+(String) string+"\"");
		    	}
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

	public void setLangOverride(int override)
	{
		langOverride = override;
	}
	
	public int getNumLangs() {
		return exhFile.getNumLanguages();
	}	
	
	public JTable getTable()
	{
		return table;
	}
	
	private void loadColumnNames(String exhname)
	{
		try{
			BufferedReader br = new BufferedReader(new FileReader(Constants.EXH_NAMES_PATH + exhname.replace("exh", "lst")));
		    for(String line; (line = br.readLine()) != null; ) {
		    	//Skip comments and whitespace
		        if (line.startsWith("#") || line.isEmpty() || line.equals(""))
		        	continue;		        
		        if (line.contains(":"))
		        {
		        	String split[] = line.split(":", 2);
		        	if (split.length != 2)
		        		continue;
		        	
		        	if (split[1].isEmpty())
		        		continue;
		        	columnNames.put(Integer.parseInt(split[0]), split[1]);
		        }
		    } 
		    br.close();
		}
		catch (IOException e){
			
		}
	}

	public EXHF_File getExhFile() {
		return exhFile;
	}
	
}
