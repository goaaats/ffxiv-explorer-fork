package ca.fraggergames.ffxivextract;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;
import ca.fraggergames.ffxivextract.helpers.PathSearcher;
import ca.fraggergames.ffxivextract.storage.HashDatabase;
import ca.fraggergames.ffxivextract.storage.PathHashList;

public class Main {

	public static void main(String[] args) {		
		
		//Init the hash database
		try {
			HashDatabase.init();			
			/*JOptionPane.showMessageDialog(null,
					"Filelist.db is missing. No file or folder names will be shown... instead the file's hashes will be displayed.",
				    "Hash DB Load Error",
				    JOptionPane.ERROR_MESSAGE);		*/
		} catch (ClassNotFoundException e1) {			
			e1.printStackTrace();
		}
		
		//Set to windows UI
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
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
					File file = new File(args[1].replace("\"", "") + "/game/sqpack/ffxiv/");
					File fileList[] = file.listFiles();
					
					for (File f : fileList)
					{
						if (f.getName().endsWith(".index"))
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
		
	}
	
}
