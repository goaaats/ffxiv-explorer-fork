package ca.fraggergames.ffxivextract;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;
import ca.fraggergames.ffxivextract.helpers.PathSearcher;
import ca.fraggergames.ffxivextract.helpers.VersionUpdater;
import ca.fraggergames.ffxivextract.helpers.VersionUpdater.VersionCheckObject;
import ca.fraggergames.ffxivextract.storage.HashDatabase;

public class Main {

	public static void main(String[] args) {		
		
		//Set to windows UI
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Init the hash database
		try {
			File dbFile = new File("./" + Constants.DBFILE_NAME);
			if (dbFile.exists())
				HashDatabase.init();
			else
				JOptionPane.showMessageDialog(null,
						Constants.DBFILE_NAME + " is missing. No file or folder names will be shown... instead the file's hashes will be displayed.",
					    "Hash DB Load Error",
					    JOptionPane.ERROR_MESSAGE);		
		} catch (ClassNotFoundException e1) {			
			e1.printStackTrace();
		}		
		
		/*
		
		try{
			for (int i = 127000; i < 128000; i+= 1000)
			{
				Connection conn = HashDatabase.getConnection();
				conn.setAutoCommit(false);
				for (int i2 = i; i2 < i+1000; i2++)
					HashDatabase.addPathToDB("ui/icon/" + String.format("%06d", i) + "/de/" + String.format("%06d", i2) + ".tex", conn);
				System.out.println(i);
				conn.commit();
				HashDatabase.closeConnection(conn);
			}
		}		
		catch (Exception e){}*/
		//EXD_Searcher.createEXDFiles("E:\\Coding\\workspace3\\FFXIV_Extractor\\exddump2.txt");
		
	/*	try {
			HashDatabase.loadPathsFromTXT("E:\\Coding\\workspace3\\FFXIV_Extractor\\exddump2.txtout.txt");
			//return;
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		/*
		SqPack_IndexFile index;
		try {
			index = new SqPack_IndexFile("E:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\0a0000.win32.index");
		
		SqPack_DatFile dat = new SqPack_DatFile("E:\\Program Files (x86)\\SquareEnix\\FINAL FANTASY XIV - A Realm Reborn\\game\\sqpack\\ffxiv\\0a0000.win32.dat0");
		EXD_Searcher.saveEXDNames(index, dat);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		//EXD_Searcher.saveEXL();
		
		//Arguments
		if (args.length>0)
		{
			//DEBUG ON
			if (args[0].equals("-debug"))
			{
				Constants.DEBUG = true;
				System.out.println("Debug Mode ON");
			}
			
			//PATHSEARCH
			if (args[0].equals("-pathsearch"))
			{
				if (args.length < 2)
				{
					System.out.println("No path to the FFXIV folder or to an index file was given.");
					return;
				}
				
				System.out.println("Starting Path Searcher (this will take a while)");				
				
				if (args[1].endsWith(".index"))
				{
					try {
						PathSearcher.doPathSearch(args[1]);
					} catch (IOException e) {
						System.out.println("There was an error searching. Stacktrace: ");
						e.printStackTrace();
					}
				}
				else
				{
					File file = new File(args[1].replace("\"", "") + "/game/sqpack/ffxiv/");
					File fileList[] = file.listFiles();
					
					for (File f : fileList)
					{												
						if ((!f.getName().contains("02") && !f.getName().contains("04") && !f.getName().contains("07")) && f.getName().endsWith(".index"))
						{
							try {
								PathSearcher.doPathSearch(f.getAbsolutePath());
								System.gc();
							} catch (IOException e) {
								System.out.println("There was an error searching. Stacktrace: ");
								e.printStackTrace();
							}
						}					
					}
				}
				
				return;
			}
		}		
		
		//Open up the main window
		FileManagerWindow fileMan = new FileManagerWindow(Constants.APPNAME);
		fileMan.setVisible(true);								
		
		//Do version check
		Preferences prefs = Preferences.userNodeForPackage(ca.fraggergames.ffxivextract.Main.class);
		boolean firstRun = prefs.getBoolean(Constants.PREF_FIRSTRUN, true);
		
		//First Run
		if (firstRun)
		{
			prefs.putBoolean(Constants.PREF_FIRSTRUN, false);			
				   
			int n = JOptionPane.showConfirmDialog(fileMan,  "Would you like FFXIV Extractor to check for a new hash database?", "Hash DB Version Check",				    
				    JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION)
			{
				prefs.putBoolean(Constants.PREF_DO_DB_UPDATE, true);
				checkForNewDb(fileMan);
			}
			else
				prefs.putBoolean(Constants.PREF_DO_DB_UPDATE, false);
		}
		
		//Version Check
		if (prefs.getBoolean(Constants.PREF_DO_DB_UPDATE, false))
			checkForNewDb(fileMan);
		
	}
	
	private static void checkForNewDb(FileManagerWindow fileMan){
		VersionCheckObject checkObj = VersionUpdater.checkForUpdates();
		
		if (Constants.DB_VERSION_CODE < checkObj.currentDbVer)
		{
			int n = JOptionPane.showConfirmDialog(fileMan,  "A new DB was found updated on " + checkObj.dbUpdateDate + ", want to download? " + (Constants.APP_VERSION_CODE < checkObj.currentAppVer ? "\nBTW, a new app version is up at " + Constants.URL_WEBSITE + "!" : ""), "New DB Found",				    
				    JOptionPane.YES_NO_OPTION);
			if (n == JOptionPane.YES_OPTION)
			{
				File file = new File("./hashlist.db");
				file.delete();
				
				BufferedInputStream in = null;
			    FileOutputStream fout = null;
			    try {
			        in = new BufferedInputStream(new URL(Constants.URL_HASHLIST_FILE).openStream());
			        fout = new FileOutputStream("./hashlist.db");

			        final byte data[] = new byte[1024];
			        int count;
			        while ((count = in.read(data, 0, 1024)) != -1) {
			            fout.write(data, 0, count);
			        }
			    } catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				
				//Init DB Again				
				try {
					File dbFile = new File("./" + Constants.DBFILE_NAME);
					if (dbFile.exists())
						HashDatabase.init();
					else
						JOptionPane.showMessageDialog(null,
								Constants.DBFILE_NAME + " is missing. No file or folder names will be shown... instead the file's hashes will be displayed.",
							    "Hash DB Load Error",
							    JOptionPane.ERROR_MESSAGE);		
				} catch (ClassNotFoundException e1) {			
					e1.printStackTrace();
				}
			}
		}
		
	}
}
