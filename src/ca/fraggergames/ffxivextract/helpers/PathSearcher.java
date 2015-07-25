package ca.fraggergames.ffxivextract.helpers;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import ca.fraggergames.ffxivextract.Constants;
import ca.fraggergames.ffxivextract.Strings;
import ca.fraggergames.ffxivextract.models.SqPack_DatFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_File;
import ca.fraggergames.ffxivextract.models.SqPack_IndexFile.SqPack_Folder;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

@SuppressWarnings("serial")
public class PathSearcher extends JFrame {
	public PathSearcher() {
	}
/*
	static String folders[] = {
			"common/",
			"bgcommon/",
			"exd/",
			"bg/",
			"music/",
			"game_script/",
			"cut/",
			"chara/",
			"ui/",
			"sound/",
			"shader/",
			"vfx/",
	};
	*/
	
	static String folders[] = {
		"bg/"
	};
	
 	public static void doPathSearch(String path) throws IOException {

		System.out.println("Opening " + path + "....");
		
		SqPack_IndexFile currentIndexFile = new SqPack_IndexFile(path, true);
		
		int numFound = 0;
		int numFoundFolder = 0;
		
		int numNewFound = 0;
		int numNewFoundFolder = 0;
		
		System.out.println("Beginning path search on " + path + "....");
		
		for (int folderIndex = 0; folderIndex < folders.length; folderIndex++)
		{
		
			System.out.println("Searching for folder " + folders[folderIndex] + "....");
			HashDatabase.beginConnection();
			try {
				HashDatabase.setAutoCommit(false);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			String string = folders[folderIndex];
			for (int i = 0; i < currentIndexFile.getPackFolders().length; i++) {
				try {
					HashDatabase.commit();
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				SqPack_Folder f = currentIndexFile.getPackFolders()[i];
				for (int j = 0; j < f.getFiles().length; j++) {
					SqPack_File fi = f.getFiles()[j];
					byte[] data;
					try {
						if (currentIndexFile.getContentType(fi.dataoffset) == 4)
							continue;
						data = currentIndexFile.extractFile(fi.dataoffset, null);
						if (data == null || (data.length >= 8 && data[0] == 'S' && data[1] == 'E' && data[2] == 'D' && data[3] == 'B' && data[4] == 'S' && data[5] == 'S' && data[6] == 'C' && data[7] == 'F'))
							continue;
						
						for (int i2 = 0; i2 < data.length - string.length(); i2++) {
							for (int j2 = 0; j2 < string.length(); j2++) {
								if (data[i2 + j2] == string.charAt(j2)) {
									if (j2 == string.length() - 1) {										
										
										//Check if this is bgcommon while looking for common
										if (folderIndex == 0 && ((data[i2-1] == 'g' && data[i2-2] == 'b') || data[i2-1] == '/'))
											break;
										
										//Look for end
										int endString = 0;
										for (int endSearch = i2; endSearch < data.length - string.length(); endSearch++)
										{
											if (data[endSearch] == 0)
											{
												endString = endSearch;
												break;
											}
										}										
	
										//Hack for last file
										if (endString == 0)
											endString = data.length-1;
										
										//Get full path
										String fullpath = new String(data, i2, endString-i2);
																				
										//Add to list
										//if (HashDatabase.getFolder(f.getId()) == null)
										//{						
											System.out.println("NEW->"+fullpath);
											numNewFound++;
											numNewFoundFolder++;
											HashDatabase.addPathToDB(fullpath, currentIndexFile.getName());										
										//}
										//else
										//{
										//	numFound++;
										//	numFoundFolder++;
										//}
																										
									} else
										continue;
								} else
									break;
							}						
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
	
				}
				
			}
			System.out.println("Found " + numFoundFolder + " paths, " + numNewFoundFolder + " were new.");
			numFoundFolder = 0;
			numNewFoundFolder = 0;
			try {
				HashDatabase.commit();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			HashDatabase.closeConnection();
		}
		System.out.println("Done search on " + path + ". Found " + numFound + " paths, " + numNewFound + " were new.");
		System.out.println("========================================");
	
	}

 	public static void addModelsFromItemsTable(String path)
 	{
 		InputStream in;
		BufferedWriter writer = null;
		
		boolean readingName = true;
		
		try {
			in  = new FileInputStream(path);
			writer = new BufferedWriter(new FileWriter("./exddump2.txt"));
			
			while(true)
			{
				int b = in.read();
				
				if (b == -1)
					break;
				
				if (b == ',' || b == 0x0D)
				{
					if (b == 0x0D)
						in.read();
					if (readingName)
						writer.append("\r\n");
					readingName = !readingName;
				}
	
				System.out.println((char)b);
				
				if (readingName)
					writer.append((char)b);
			}
			in.close();
			writer.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
 	}
 	
}
