package ca.fraggergames.ffxivextract;

import javax.swing.UIManager;

import ca.fraggergames.ffxivextract.gui.FileManagerWindow;
import ca.fraggergames.ffxivextract.helpers.LERandomAccessFile;
import ca.fraggergames.ffxivextract.helpers.LuaDec;
import ca.fraggergames.ffxivextract.models.Log_File;
import ca.fraggergames.ffxivextract.models.SCD_File;

public class Main {

	public static void main(String[] args) {		
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		FileManagerWindow fileMan = new FileManagerWindow(Constants.APPNAME);
		fileMan.setVisible(true);		
		
		try{
			Log_File log = new Log_File("C:\\Users\\Filip\\Documents\\my games\\FINAL FANTASY XIV - A Realm Reborn\\FFXIV_CHR00400000009B1AC9\\log\\00000003.log");
			}
			catch (Exception e){}
	}

	
}
