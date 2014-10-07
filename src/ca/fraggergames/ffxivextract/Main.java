package ca.fraggergames.ffxivextract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.helpers.LuaDec;
import ca.fraggergames.ffxivextract.helpers.PathSearcher;
import ca.fraggergames.ffxivextract.models.Log_File;
import ca.fraggergames.ffxivextract.models.SCD_File;
import ca.fraggergames.ffxivextract.storage.PathHashList;

public class Main {

	public static void main(String[] args) {		
		
		//Load in the hash database
		try {						
			long oldTime = System.currentTimeMillis();
			Constants.hashDatabase = PathHashList.loadDB("C:\\Users\\Filip\\Desktop\\filelist_notcompressed.db");
			long newTime = System.currentTimeMillis();
			if (Constants.DEBUG)
				System.out.println("Loaded: " + Constants.hashDatabase.getNumFiles() + " files and " + Constants.hashDatabase.getNumFolders() + " folders in " + (newTime-oldTime) + "ms.");
		} catch (FileNotFoundException e) {
			JOptionPane.showMessageDialog(null,
						"Filelist.db is missing. No file or folder names will be shown... instead the file's hashes will be displayed.",
					    "Hash DB Load Error",
					    JOptionPane.ERROR_MESSAGE);		
		}	
		
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
					File file = new File(args[1] + "game/sqpack/ffxiv/");
					File fileList[] = file.listFiles();
					
					for (File f : fileList)
					{
						if (f.getName().endsWith(".index"))
						{
							try {
								PathSearcher.doPathSearch(f.getAbsolutePath());
							} catch (IOException e) {
								System.out.println("There was an error searching. Stacktrace: ");
								e.printStackTrace();
							}
						}					
					}
				}
				
				System.out.println("Path Searcher complete, saving database.");
				try {
					Constants.hashDatabase.saveDB("./filelist.db");
				} catch (FileNotFoundException e) {
					System.out.println("There was an error saving the database. Stacktrace: ");
					e.printStackTrace();
				}
				
				return;
			}
		}
				
		//Set to windows UI
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//Open up the main window
		FileManagerWindow fileMan = new FileManagerWindow(Constants.APPNAME);
		fileMan.setVisible(true);								
		
	}
	
}
