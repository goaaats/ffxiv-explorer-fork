package ca.fraggergames.ffxivextract;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.helpers.LuaDec;
import ca.fraggergames.ffxivextract.models.Log_File;
import ca.fraggergames.ffxivextract.models.SCD_File;
import ca.fraggergames.ffxivextract.storage.PathHashList;

public class Main {

	public static void main(String[] args) {		
		
		//Arguments
		if (args.length>0)
		{
			if (args[0].equals("-debug"))
			{
				Constants.DEBUG = true;
				System.out.println("Debug Mode ON");
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
		
	}

	
}
