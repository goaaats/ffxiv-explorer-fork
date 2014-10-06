package ca.fraggergames.ffxivextract;

import java.io.IOException;

import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.helpers.LuaDec;
import ca.fraggergames.ffxivextract.models.Log_File;
import ca.fraggergames.ffxivextract.models.SCD_File;
import ca.fraggergames.ffxivextract.storage.PathHashList;

public class Main {

	public static void main(String[] args) {		
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileManagerWindow fileMan = new FileManagerWindow(Constants.APPNAME);
		fileMan.setVisible(true);						
		
		try {
			long oldTime = System.currentTimeMillis();
			Constants.hashDatabase = PathHashList.loadDB("C:\\Users\\Filip\\Desktop\\filelist2.db");
			long newTime = System.currentTimeMillis();
			System.out.println("Loaded: " + Constants.hashDatabase.getNumFiles() + " files and " + Constants.hashDatabase.getNumFolders() + " folders in " + (newTime-oldTime) + "ms.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
